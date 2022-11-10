package nl.knaw.dans.verifydataset.core.rule;

import nl.knaw.dans.lib.dataverse.model.dataset.SingleValueField;
import nl.knaw.dans.verifydataset.core.config.AuthorNameFormatOkConfig;

import java.util.Map;

public class AuthorNameFormatOk extends MetadataRule {
    String regexp;

    public AuthorNameFormatOk(AuthorNameFormatOkConfig authorNameFormatOkConfig) {
        blockName = "citation";
        fieldName = "author";
        regexp = authorNameFormatOkConfig.getRegexp();
    }

    @Override
    protected String verifySingleField(Map<String, SingleValueField> attributes) {
        String name = attributes.getOrDefault("authorName", defaultAttribute).getValue();
        if (name == null || name.matches(regexp))
            return "";
        else
            return String.format("author name '%s' does not match %s", name, regexp);
    }
}
