package currency.currencyconverter;


public class Currency {
    private String _id;
    private String _name;

    public Currency(String id, String name){
        _id = id;
        _name = name;
    }



    public String getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    @Override
    public String toString() {
        return _name;
    }
}
