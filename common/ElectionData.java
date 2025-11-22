package common;

import java.util.List;

public class ElectionData extends NetProtocol {

    private static final long serialVersionUID = 1L;

    private final String question;
    private final List<String> options;

    public ElectionData(String question, List<String> options) {
        this.question = question;
        this.options = options;
    }
    
    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }
}