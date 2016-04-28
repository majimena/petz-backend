package org.majimena.petical.web.api;

import com.codahale.metrics.annotation.Timed;
import org.majimena.petical.domain.user.PasswordRegistry;
import org.majimena.petical.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * パスワードをリセットするコントローラ.
 */
@RestController
@RequestMapping("/api/v1")
public class PasswordResetController {

    /**
     * ユーザーサービス.
     */
    @Inject
    private UserService userService;

    /**
     * パスワードをリセットする要求を出します.<br/>
     * この処理をすると、パスワードをリセットするためのキーを記載したメールを送信します.
     *
     * @param mail パスワードをリセットするユーザーのログインIDまたは登録メールアドレス
     * @return リクエストエンティティ（成功時：200、失敗時：404、異常終了時：500）
     */
    @Timed
    @RequestMapping(value = "/password", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@RequestBody String mail) {
        return userService.requestPasswordReset(mail)
            .map(user -> ResponseEntity.ok().build())
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * パスワードをリセットします.<br/>
     * パスワードをリセットするキーは送信してから１時間のみ有効です.
     *
     * @param registry パスワードレジストリ
     * @return リクエストエンティティ（成功時：200、失敗時：404、異常終了時：500）
     */
    @Timed
    @RequestMapping(value = "/me/password", method = RequestMethod.POST)
    public ResponseEntity<Void> post(@RequestBody PasswordRegistry registry) {
        return userService.resetPassword(registry.getNewPassword(), registry.getResetKey())
            .map(user -> ResponseEntity.ok().build())
            .orElse(ResponseEntity.notFound().build());
    }
}
