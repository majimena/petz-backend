package org.majimena.petz.web.api.ticket;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import org.majimena.petz.domain.Ticket;
import org.majimena.petz.domain.ticket.TicketCriteria;
import org.majimena.petz.security.SecurityUtils;
import org.majimena.petz.service.TicketService;
import org.majimena.petz.web.utils.ErrorsUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;

/**
 * チケットコントローラ.
 */
@RestController
@RequestMapping("/api/v1")
public class TicketController {

    /**
     * チケットサービス.
     */
    @Inject
    private TicketService ticketService;

    /**
     * チケットバリデータ.
     */
    @Inject
    private TicketValidator ticketValidator;

    /**
     * チケットサービスを設定する.
     *
     * @param ticketService チケットサービス
     */
    public void setTicketService(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * チケットバリデータを設定する.
     *
     * @param ticketValidator チケットバリデータ
     */
    public void setTicketValidator(TicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
    }

    /**
     * ログインユーザの診察チケットを取得する.
     *
     * @param criteria チケットクライテリア
     * @return 該当するチケット一覧
     */
    @Timed
    @RequestMapping(value = "/tickets", method = RequestMethod.GET)
    public ResponseEntity<List<Ticket>> get(@Valid TicketCriteria criteria) {
        // ログインユーザIDで検索する
        String userId = SecurityUtils.getCurrentUserId();
        criteria.setUserId(userId);

        // 月単位でチケットを取得する
        List<Ticket> tickets = Lists.newArrayList();
        ticketService.getTicketsByTicketCriteria(criteria).stream().forEach(ticket -> {
            // クライアント側の処理速度向上のため、余計な値は返さない
            if (ticket.getCustomer() != null) {
                ticket.getCustomer().setClinic(null);
                ticket.getCustomer().setUser(null);
            }
            if (ticket.getChart() != null) {
                ticket.getChart().setClinic(null);
                ticket.getChart().setCustomer(null);
                ticket.getChart().setPet(null);
            }
            ticket.getPet().setUser(null);
        });
        return ResponseEntity.ok().body(tickets);
    }

    /**
     * チケットを新規作成する.
     *
     * @param ticket 登録するチケット情報
     * @param errors エラーオブジェクト
     * @return 登録したチケット
     * @throws BindException 入力内容に不備がある場合
     */
    @Timed
    @RequestMapping(value = "/tickets", method = RequestMethod.POST)
    public ResponseEntity<Ticket> post(@RequestBody @Valid Ticket ticket, BindingResult errors) throws BindException {
        // カスタムバリデーションを行う
        ticketValidator.validate(ticket, errors);
        ErrorsUtils.throwIfHaveErrors(errors);

        // チケットを保存する
        Ticket created = ticketService.saveTicket(ticket);
        return ResponseEntity.created(
                URI.create("/api/v1/tickets/" + created.getId())).body(created);
    }

    /**
     * チケットを更新する.
     *
     * @param ticketId チケットID
     * @param ticket   更新するチケット情報
     * @param errors   エラーオブジェクト
     * @return 更新したチケット
     * @throws BindException 入力内容に不備がある場合
     */
    @Timed
    @RequestMapping(value = "/tickets/{ticketId}", method = RequestMethod.PUT)
    public ResponseEntity<Ticket> put(@PathVariable String ticketId, @RequestBody @Valid Ticket ticket, BindingResult errors) throws BindException {
        // ユーザ権限のチェックとIDのコード体系チェック
        SecurityUtils.throwIfNotCurrentUser(ticket.getPet().getUser().getId());
        ErrorsUtils.throwIfNotIdentify(ticketId);

        // カスタムバリデーションを行う
        ticketValidator.validate(ticket, errors);
        ErrorsUtils.throwIfHaveErrors(errors);

        // チケットを更新する
        Ticket created = ticketService.updateTicket(ticket);
        return ResponseEntity.ok().body(created);
    }

    /**
     * チケットを削除する.
     *
     * @param ticketId チケットID
     * @return レスポンスステータス（200）
     */
    @Timed
    @RequestMapping(value = "/tickets/{ticketId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String ticketId) {
        // TODO ユーザ権限のチェックとIDのコード体系チェック
//        SecurityUtils.throwIfNotCurrentUser(schedule.getUser().getId());
        ErrorsUtils.throwIfNotIdentify(ticketId);

        // チケットを更新する
        ticketService.deleteTicketByTicketId(ticketId);
        return ResponseEntity.ok().build();
    }
}