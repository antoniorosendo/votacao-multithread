package common;
public class Vote extends NetProtocol {

    private static final long serialVersionUID = 1L;

    private final String cpf;
    private final String chosenOption;

    /**
     * Constructs a new Vote.
     * @param cpf The voter's validated CPF.
     * @param chosenOption The option string the user selected.
     */
    public Vote(String cpf, String chosenOption) {
        this.cpf = cpf;
        this.chosenOption = chosenOption;
    }

    public String getCpf() {
        return cpf;
    }

    public String getChosenOption() {
        return chosenOption;
    }
}