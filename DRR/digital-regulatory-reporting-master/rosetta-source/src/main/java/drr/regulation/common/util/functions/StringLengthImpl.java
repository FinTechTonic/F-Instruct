package drr.regulation.common.util.functions;

import java.util.Optional;

public class StringLengthImpl extends StringLength {

    @Override
    protected Integer doEvaluate(String str) {
        return Optional.ofNullable(str).map(String::length).orElse(0);
    }
}
