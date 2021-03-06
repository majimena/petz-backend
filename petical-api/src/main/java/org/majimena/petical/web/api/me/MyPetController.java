package org.majimena.petical.web.api.me;

import com.codahale.metrics.annotation.Timed;
import org.majimena.petical.domain.Pet;
import org.majimena.petical.domain.pet.PetCriteria;
import org.majimena.petical.security.SecurityUtils;
import org.majimena.petical.service.PetService;
import org.majimena.petical.web.utils.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.List;

/**
 * マイペットのコントローラ.
 */
@RestController
@RequestMapping("/api/v1")
public class MyPetController {

    /**
     * ペットサービス.
     */
    @Inject
    private PetService petService;

    /**
     * マイペットを取得する.
     *
     * @param offset ページング時のオフセット値
     * @param limit  １ページのサイズ
     * @return レスポンスエンティティ（通常時は200）
     * @throws URISyntaxException 通常発生しない例外
     */
    @Timed
    @RequestMapping(value = "/me/pets", method = RequestMethod.GET)
    public ResponseEntity<List<Pet>> getAll(@RequestParam(value = "page", required = false) Integer offset,
                                            @RequestParam(value = "per_page", required = false) Integer limit) throws URISyntaxException {
        // TODO ログインしていなければ、空を返すようにする
        // 検索条件を生成
        PetCriteria criteria = new PetCriteria();
        criteria.setUserId(SecurityUtils.getCurrentUserId());

        // ページング要素を加えて検索結果を返す
        Pageable pageable = PaginationUtils.generatePageRequest(offset, limit);
        Page<Pet> page = petService.getPetsByPetCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(page, "/api/v1/me/pets", offset, limit);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
