package drr.regulation.common.util.functions;

import java.util.Optional;

public class StringContainsImpl extends StringContains {

    @Override
    protected Boolean doEvaluate(String input, String regex) {
        return Optional.ofNullable(input)
                .map(s -> s.matches(regex))
                .orElse(false);
    }
}