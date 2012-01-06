package it.geosolutions.tools.commons.check;

public final class Objects {
    /**
     * Checks if the input is not null.
     * 
     * @param oList
     *            list of elements to check for null.
     */
    public static void notNull(Object... oList) {
        for (Object o : oList)
            if (o == null)
                throw new NullPointerException("Input objects cannot be null");

    }
}
