(:test)
function ArrayzTest_addItem(logger) {
    var inputArray = ["item1", "item2", "item3"];
    var additionalItem = "item4";

    var result = Arrayz.addItem(inputArray, additionalItem);

    logger.debug("expectedArraySize: " + (inputArray.size() + 1));
    logger.debug("actualArraySize: " + result.size());

    logger.debug("expectedLastItem: " + additionalItem);
    logger.debug("actualLastItem: " + result[result.size() - 1]);

    return result.size() == inputArray.size() + 1
        && result[result.size() - 1].equals(additionalItem);
}

(:test)
function ArrayzTest_join(logger) {
    var inputArray1 = [ "item1", "item2" ];
    var inputArray2 = [ "item3", "item4" ];

    var result = Arrayz.join(inputArray1, inputArray2);

    logger.debug("expectedArraySize: " + (inputArray1.size() + inputArray2.size()));
    logger.debug("actualArraySize: " + result.size());

    logger.debug("expectedFirstItem: " + inputArray1[0]);
    logger.debug("actualFirstItem: " + result[0]);

    logger.debug("expectedLastItem: " + inputArray2[inputArray2.size() - 1]);
    logger.debug("actualLastItem: " + result[result.size() - 1]);

    return result.size() == inputArray1.size() + inputArray2.size()
        && inputArray1[0].equals(result[0])
        && inputArray2[inputArray2.size() - 1].equals(result[result.size() - 1]);
}
