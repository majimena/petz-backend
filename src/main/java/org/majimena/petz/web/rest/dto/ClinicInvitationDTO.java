package org.majimena.petz.web.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * クリニック招待状リクエスト.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicInvitationDTO implements Serializable {

    @NotNull
    @Size(min = 1, max = 100)
    private String[] emails;

}
