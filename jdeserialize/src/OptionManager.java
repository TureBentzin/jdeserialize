package org.unsynchronized;

import java.io.Serial;
import java.util.*;

/**
 * Simple getopt()-like implementation.
 */
public class OptionManager {
    private final Map<String, Integer> optionLength;
    private List<String> fileArguments;
    private final Map<String, String> descriptions;
    private Map<String, List<String>> optionValues;

    public static class OptionParseException extends Exception {
        @Serial
        private static final long serialVersionUID = 2898924890585885551L;

        public OptionParseException(String message) {
            super(message);
        }
    }

    /**
     * Gets the list arguments specified that were *not* options or their arguments, in
     * the order they were specified.
     *
     * @return the list of non-option String arguments
     */
    public List<String> getFileArguments() {
        return fileArguments;
    }

    /**
     * Gets the set of all options specified, as well as the list of their arguments.
     *
     * @return a map of all options specified; values are lists of arguments
     */
    public Map<String, List<String>> getOptionValues() {
        return optionValues;
    }

    /**
     * Constructor.  
     *
     * @param optionLength Map of options to parse.  The key should be an option string (including
     * any initial dashes), and the value should be an Integer representing the number of
     * arguments to parse following the option.
     * @param descriptions Map of option descriptions.
     */
    public OptionManager(Map<String, Integer> optionLength, Map<String, String> descriptions) {
        this.optionLength = optionLength;
        this.descriptions = descriptions;
    }

    /**
     * Constructor.  
     */
    public OptionManager() {
        this.optionLength = new HashMap<>();
        this.descriptions = new HashMap<>();
    }

    /**
     * Determines whether or not the option was specified when the arguments were parsed.
     *
     * @return true iff the argument was specified (with the correct number of arguments).
     */
    public boolean hasOption(String option) {
        return optionValues.containsKey(option);
    }

    /**
     * Gets the list of arguments for a given option, or null if the option wasn't
     * specified.
     *
     * @param option the option 
     * @return the list of arguments for option
     */
    public List<String> getArguments(String option) {
        return optionValues.get(option);
    }

    /**
     * Add an option to the internal set, including the number of arguments and the
     * description. 
     *
     * @param option option string, including any leading dashes
     * @param arguments number of arguments
     * @param description description of the option
     */
    public void addOption(String option, int arguments, String description) {
        optionLength.put(option, arguments);
        descriptions.put(option, description);
    }

    /**
     * Do the parsing/validation.
     * @param args arguments to parse
     * @throws OptionParseException if a parse error occurs (the exception message will
     * have details)
     */
    public void parse(String[] args) throws OptionParseException {
        this.fileArguments = new ArrayList<>();
        this.optionValues = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            Integer length = optionLength.get(args[i]);
            if (length != null) {
                ArrayList<String> argumentValues = new ArrayList<>(length);
                for (int j = 0; j < length; j++) {
                    if (i + 1 + j >= args.length) {
                        throw new OptionParseException("expected " + length + " arguments after " + args[i]);
                    }
                    argumentValues.add(args[i + 1 + j]);
                }
                List<String> values = optionValues.get(args[i]);
                if (values == null) {
                    // create a new entry
                    optionValues.put(args[i], argumentValues);
                } else {
                    values.addAll(argumentValues);
                }
                i += length;
                continue;
            }
            fileArguments.add(args[i]);
        }
    }

    /**
     * Get a tabular description of all options and their descriptions, one per line.
     */
    public String getDescriptionString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (optionLength != null && !optionLength.isEmpty()) {
            stringBuilder.append("Options:").append(System.lineSeparator());
            TreeSet<String> opts = new TreeSet<>(this.optionLength.keySet());
            for (String opt : opts) {
                stringBuilder.append("    ").append(opt);
                for (int i = 0; i < optionLength.get(opt); i++) {
                    stringBuilder.append(" arg").append(i + 1);
                }
                stringBuilder.append(": ").append(descriptions.get(opt)).append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }
}
