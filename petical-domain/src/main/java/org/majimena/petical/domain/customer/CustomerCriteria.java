package org.majimena.petical.domain.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.majimena.petical.datatype.defs.ID;
import org.majimena.petical.datatype.defs.MailAddress;
import org.majimena.petical.datatype.defs.Name;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 顧客クライテリア.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCriteria implements Serializable {

    /**
     * クリニックID.
     */
    @Size(max = ID.MAX_LENGTH)
    private String clinicId;

    /**
     * ログインメールアドレス.
     */
    @Email
    @Size(max = MailAddress.MAX_LENGTH)
    private String login;

    /**
     * メールアドレス.
     */
    @Email
    @Size(max = MailAddress.MAX_LENGTH)
    private String email;

    /**
     * 名前.
     */
    @Size(max = Name.MAX_LENGTH)
    private String name;
}
