package com.lombardrisk.ignis.client.external.pipeline.view;

import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class JoinView {

    private Long id;
    private SchemaDetailsView left;
    private SchemaDetailsView right;
    private FieldView leftField;
    private FieldView rightField;
}
