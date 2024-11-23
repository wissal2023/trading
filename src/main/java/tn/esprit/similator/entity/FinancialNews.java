package tn.esprit.similator.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinancialNews {
    private String date;
    private String title;
    private String content;
    private String link;
    private List<String> symbols;
}
