package br.eti.x87.property;

import android.content.Context;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

import br.eti.x87.xmlParser.ParserProperties;

public class ListPropertiesFromFile implements ListProperties {

    private Context mContext;

    public ListPropertiesFromFile(Context context){
        this.mContext = context;
    }

    public CharSequence[] getPropertiesList() {
        /*
        Faz a leitura do arquivo com as propriedades e exporta para um array de char.
         */
        try {
            InputStream isProperty = mContext.getAssets().open("properties.xml");
            ParserProperties pp = new ParserProperties();
            CharSequence[] items = pp.getProperties(isProperty);
            return items;
        } catch (Exception e) {
            Toast.makeText(mContext, "Erro ao carregar propriedades!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public ArrayList<String> getPropertiesListArray() {
        /*
        Faz a leitura do arquivo com as propriedades e exporta para um array.
         */
        try {
            InputStream isProperty = mContext.getAssets().open("properties.xml");
            ParserProperties pp = new ParserProperties();
            ArrayList<String> items = pp.getPropertiesArrayList(isProperty);
            return items;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Erro ao carregar propriedades!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

}
