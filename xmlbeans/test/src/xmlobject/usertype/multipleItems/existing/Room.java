package xmlobject.usertype.multipleItems.existing;

public class Room
{
    private int digits;
    private String letters;


    public Room()
    {
    }

    public Room(int digits, String letters)
    {
        setDigits(digits);
        setLetters(letters);
    }

    public int getDigits()
    {
        return digits;
    }

    public void setDigits(int digits)
    {
        if (digits > 999 || digits < 0)
            throw new IllegalArgumentException("bad digits");
        this.digits = digits;
    }

    public String getLetters()
    {
        return letters;
    }

    public void setLetters(String letters)
    {
        if (letters == null || letters.length() != 2)
            throw new IllegalArgumentException("bad letters");
        this.letters = letters;
    }
}
