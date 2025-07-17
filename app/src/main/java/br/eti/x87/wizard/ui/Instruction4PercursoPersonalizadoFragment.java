package br.eti.x87.wizard.ui;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.eti.x87.snac.R;
import br.eti.x87.wizard.model.Instruction4PercursoPersonalizadoPage;
import br.eti.x87.wizard.model.Page;

public class Instruction4PercursoPersonalizadoFragment extends Fragment {
    private static final String ARG_KEY = "key";

    private PageFragmentCallbacks mCallbacks;
    private List<String> mChoices;
    private String mKey;
    private Page mPage;

    public static Instruction4PercursoPersonalizadoFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        Instruction4PercursoPersonalizadoFragment fragment = new Instruction4PercursoPersonalizadoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public Instruction4PercursoPersonalizadoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mKey = args.getString(ARG_KEY);
        mPage = mCallbacks.onGetPage(mKey);

        Instruction4PercursoPersonalizadoPage fixedChoicePage = (Instruction4PercursoPersonalizadoPage) mPage;
        mChoices = new ArrayList<String>();
        for (int i = 0; i < fixedChoicePage.getOptionCount(); i++) {
            mChoices.add(fixedChoicePage.getOptionAt(i));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_instruction_percurso_personalizado, container, false);
        ((TextView) rootView.findViewById(R.id.testingID)).setText("Personalizando o percurso");

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }

        mCallbacks = (PageFragmentCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

}
