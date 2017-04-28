(:test)
function StringzTest_reverse(logger) {
    var input = "reversed";

    var expected = "desrever";
    var actual = Stringz.reverse(input);

    logger.debug("expected: " + expected);
    logger.debug("actual: " + actual);

    return expected.equals(actual);
}

(:test)
function StringzTest_split(logger) {
    var firstInputElement = "unit";
    var secondInputElement = "test";
    var separator = "|";

    var input = firstInputElement + separator + secondInputElement;
    var expectedArraySize = 2;
    var actual = Stringz.split(input, separator);

    logger.debug("expectedArraySize: " + expectedArraySize);
    logger.debug("actualArraySize: " + actual.size());

    logger.debug("expectedFirstElement: " + firstInputElement);
    logger.debug("actualFirstElement: " + actual[0]);

    logger.debug("expectedSecondElement: " + secondInputElement);
    logger.debug("actualSecondElement: " + actual[1]);

    return expectedArraySize == actual.size()
        && firstInputElement.equals(actual[0])
        && secondInputElement.equals(actual[1]);
}

(:test)
function StringzTest_join(logger) {
    var input = [ "This", "is", "a", "test." ];
    var expected = "This|is|a|test.";
    var actual = Stringz.join(input, "|");

    logger.debug("expected: " + expected);
    logger.debug("actual: " + actual);

    return expected.equals(actual);
}

(:test)
function StringzTest_wrap(logger) {
    var input = "This is a test.";
    var expected = "This\nis\na\ntest.";
    var actual = Stringz.wrap(input);

    logger.debug("expected: " + expected);
    logger.debug("actual: " + actual);

    return expected.equals(actual);
}
