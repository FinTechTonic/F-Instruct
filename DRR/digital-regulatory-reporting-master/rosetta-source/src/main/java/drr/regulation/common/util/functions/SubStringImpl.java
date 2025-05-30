package drr.regulation.common.util.functions;


import java.util.Optional;

public class SubStringImpl extends SubString {

    @Override
    protected String doEvaluate(String text, Integer startIndex, Integer length) {
        int start = Optional.ofNullable(startIndex).orElse(1) - 1;
        if (text == null || length == null || length < 0 || start < 0) {
            return text;
        }
        // Java start index is zero-based
        int actualLength = Math.min(length + start, text.length());
        return text.substring(start, actualLength);
    }
}
