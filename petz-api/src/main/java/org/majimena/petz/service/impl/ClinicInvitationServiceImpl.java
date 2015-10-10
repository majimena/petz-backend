package org.majimena.petz.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.majimena.petz.common.aws.AmazonSESService;
import org.majimena.petz.common.exceptions.ApplicationException;
import org.majimena.petz.common.exceptions.ResourceCannotAccessException;
import org.majimena.petz.common.exceptions.SystemException;
import org.majimena.petz.common.utils.RandomUtils;
import org.majimena.petz.domain.Clinic;
import org.majimena.petz.domain.ClinicInvitation;
import org.majimena.petz.domain.ClinicStaff;
import org.majimena.petz.domain.User;
import org.majimena.petz.domain.errors.ErrorCode;
import org.majimena.petz.domain.user.Roles;
import org.majimena.petz.repository.ClinicInvitationRepository;
import org.majimena.petz.repository.ClinicRepository;
import org.majimena.petz.repository.ClinicStaffRepository;
import org.majimena.petz.repository.UserRepository;
import org.majimena.petz.security.SecurityUtils;
import org.majimena.petz.service.ClinicInvitationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Created by todoken on 2015/06/26.
 */
@Service
@Transactional
public class ClinicInvitationServiceImpl implements ClinicInvitationService {

    @Inject
    private ClinicRepository clinicRepository;

    @Inject
    private ClinicInvitationRepository clinicInvitationRepository;

    @Inject
    private ClinicStaffRepository clinicStaffRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private SpringTemplateEngine templateEngine;

    @Inject
    private AmazonSESService amazonSESService;

    @Value("${mail.from:noreply@petz.io}")
    private String fromEmail;

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setClinicRepository(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    public void setClinicInvitationRepository(ClinicInvitationRepository clinicInvitationRepository) {
        this.clinicInvitationRepository = clinicInvitationRepository;
    }

    public void setClinicStaffRepository(ClinicStaffRepository clinicStaffRepository) {
        this.clinicStaffRepository = clinicStaffRepository;
    }

    public void setTemplateEngine(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void setAmazonSESService(AmazonSESService amazonSESService) {
        this.amazonSESService = amazonSESService;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClinicInvitation findClinicInvitationById(String invitationId) {
        ClinicInvitation invitation = clinicInvitationRepository.findOne(invitationId);
        if (invitation == null) {
            throw new ResourceCannotAccessException();
        }

        invitation.getUser().getAuthorities().size();
        return invitation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void inviteStaff(String clinicId, Set<String> emails) {
        String loginId = SecurityUtils.getCurrentLogin();
        Optional<User> login = userRepository.findOneByLogin(loginId);

        emails.stream().forEach(email -> {
            // クリニックへの招待待ちとして保存
            String activationKey = RandomUtils.generateActivationKey();
            Clinic clinic = clinicRepository.findOne(clinicId);
            ClinicInvitation invitation = ClinicInvitation.builder()
                .clinic(clinic)
                .user(login.orElseThrow(() -> new SystemException("get unsaved user.")))
                .email(email)
                .activationKey(activationKey).build();
            clinicInvitationRepository.save(invitation);

            // 招待メールを送信
            String locale = login.map(User::getLangKey).orElse("en");
            Context context = new Context(Locale.forLanguageTag(locale));
            context.setVariable("email", email);
            context.setVariable("clinic", clinic);
            context.setVariable("user", login.get());
            context.setVariable("invitation", invitation);
            context.setVariable("activationKey", activationKey);
            String subject = templateEngine.process("ClinicInvitation-subject", context);
            String content = templateEngine.process("ClinicInvitation-content", context);
            amazonSESService.send(email, fromEmail, subject, content);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activate(String invitationId, String activationKey) {
        String login = SecurityUtils.getCurrentLogin();
        ClinicInvitation invitation = clinicInvitationRepository.findOne(invitationId);

        // 招待を承認してアクティベートする（validation済みであること）
        Optional<User> user = userRepository.findOneByLogin(login);
        ClinicStaff staff = ClinicStaff.builder()
            .clinic(invitation.getClinic())
            .user(user.orElseThrow(() -> new SystemException("get unsaved user.")))
            .role(Roles.ROLE_STAFF.name())
            .activatedDate(LocalDate.now()).build();

        // アクティベーションキーの入力間違いがないかチェックする
        if (!StringUtils.equals(activationKey, invitation.getActivationKey())) {
            throw new ApplicationException(ErrorCode.PTZ_001203);
        }

        clinicStaffRepository.save(staff);
        clinicInvitationRepository.delete(invitation);
    }
}
