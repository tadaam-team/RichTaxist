package tt.richTaxist;

/**
 * Created by Tau on 31.07.2015.
 */
public enum TypeOfInput {
    BUTTON (0, R.string.button),
    SPINNER (1, R.string.spinner),
    TEXT_INPUT (2, R.string.textInput);

    public final int id;
    private final int captionId;

    TypeOfInput(int id, int captionId) {
        this.id = id;
        this.captionId = captionId;
    }

    public static TypeOfInput getById(int id){
        for (TypeOfInput x: TypeOfInput.values()){
            if (x.id==id) return x;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return MainActivity.context.getString(captionId);
    }
}
