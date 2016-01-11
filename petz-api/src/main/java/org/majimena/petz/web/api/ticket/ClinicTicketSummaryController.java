package org.majimena.petz.web.api.ticket;

import com.codahale.metrics.annotation.Timed;
import org.majimena.petz.domain.graph.Graph;
import org.majimena.petz.security.SecurityUtils;
import org.majimena.petz.service.TicketService;
import org.majimena.petz.web.utils.ErrorsUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * クリニックチケットのサマリのコントローラ.
 */
@RestController
@RequestMapping("/api/v1")
public class ClinicTicketSummaryController {

    /**
     * チケットサービス.
     */
    @Inject
    private TicketService ticketService;

    /**
     * クリニックのチケットのサマリを取得する.
     *
     * @param clinicId クリニックID
     * @return レスポンスステータス（正常時は200、権限エラー時は401）
     */
    @Timed
    @RequestMapping(value = "/clinics/{clinicId}/tickets/summary/daily", method = RequestMethod.GET)
    public ResponseEntity<Graph> get(@PathVariable String clinicId) {
        // クリニック権限のチェック
        ErrorsUtils.throwIfNotIdentify(clinicId);
        SecurityUtils.throwIfDoNotHaveClinicRoles(clinicId);

        // 本日分のチケットのサマリを取得
        Graph graph = ticketService.getTodaysTicketGraphByClinicId(clinicId);
        return ResponseEntity.ok().body(graph);
    }
}