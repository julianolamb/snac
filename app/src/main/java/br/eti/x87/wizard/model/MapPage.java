package br.eti.x87.wizard.model;

import android.content.Context;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import br.eti.x87.wizard.ui.MapaFragment;
import br.eti.x87.wizard.ui.PageFragmentCallbacks;

public class MapPage extends Page {

    public static final String PONTO_SELECIONADO = "Ponto 1";
    private Page mPage;
    private int flag;
    private Context context;

    private static final String ARG_KEY = "key";
    private PageFragmentCallbacks mCallbacks;
    private String mKey;

    public MapPage(ModelCallbacks callbacks, String title, int flag, Context context) {
        super(callbacks, title);
        this.flag = flag;
        this.context = context;
    }

    @Override
    public Fragment createFragment() {
        return MapaFragment.create(getKey(), flag, context);
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem(getTitle(), mData.getString(SIMPLE_DATA_KEY), getKey()));
    }
}