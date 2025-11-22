package common;

public class Vote extends NetProtocol {

    private static final long serialVersionUID = 1L;

    private final String cpf;
    private final String chosenOption;

    public Vote(String cpf, String chosenOption) {
        this.cpf = cpf != null ? cpf.replaceAll("\\D", "") : "";
        this.chosenOption = chosenOption;
    }

    public String getCpf() {
        return cpf;
    }

    public String getMaskedCpf() {
        if (cpf == null || cpf.length() != 11) {
            return "***";
        }
        return "***." + cpf.substring(3, 6) + ".***-**";
    }

    public String getFormattedCpf() {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + 
               cpf.substring(3, 6) + "." + 
               cpf.substring(6, 9) + "-" + 
               cpf.substring(9, 11);
    }

    public String getChosenOption() {
        return chosenOption;
    }
}