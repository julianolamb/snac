/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.eti.x87.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.eti.x87.dialog.FragmentoDialogo;
import br.eti.x87.model.Coordinate;
import br.eti.x87.navigator.Navigator;
import br.eti.x87.points.ListPoints;
import br.eti.x87.points.ListPointsFromFile;
import br.eti.x87.points.PointsHandler;
import br.eti.x87.property.ListProperties;
import br.eti.x87.property.ListPropertiesFromFile;
import br.eti.x87.snac.R;
import br.eti.x87.wizard.Wizard;
import br.eti.x87.wizard.model.AbstractWizardModel;
import br.eti.x87.wizard.model.ModelCallbacks;
import br.eti.x87.wizard.model.Page;
import br.eti.x87.wizard.ui.PageFragmentCallbacks;
import br.eti.x87.wizard.ui.ReviewFragment;
import br.eti.x87.wizard.ui.StepPagerStrip;

public class MainActivity extends FragmentActivity implements
        FragmentoDialogo.NoticeDialogListener,
        PageFragmentCallbacks,
        ReviewFragment.Callbacks,
        ModelCallbacks {
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    private boolean mEditingAfterReview;


    private AbstractWizardModel mWizardModel;

    private boolean mConsumePageSelectedEvent;

    private Button mNextButton;
    private Button mPrevButton;

    private List<Page> mCurrentPageSequence;
    private StepPagerStrip mStepPagerStrip;

    private String nomePropriedade = "";
    private String tipoPercurso = "";
    private String pontoPartida = "";

    public void setNomePropriedade(String nomePropriedade) {
        this.nomePropriedade = nomePropriedade;
    }

    public void setTipoPercurso(String tipoPercurso) {
        this.tipoPercurso = tipoPercurso;
    }

    public void setPontoPartida(String pontoPartida) {
        this.pontoPartida = pontoPartida;
    }

    public String getNomePropriedade() {
        return nomePropriedade;
    }

    public String getTipoPercurso() {
        return tipoPercurso;
    }

    public String getPontoPartida() {
        return pontoPartida;
    }

    public String findNomePropriedade(){
        ListProperties lpp = new ListPropertiesFromFile(this);
        List<String> minhaLista = lpp.getPropertiesListArray();

        List<Page> listaPaginas = mWizardModel.getCurrentPageSequence();
        for (Page p : listaPaginas)
            for (int i = 0; i < minhaLista.size(); i++)
                if (p.getData().toString().contains(minhaLista.get(i)))
                    return p.getData().toString();

        return null; // retorna nulo se não encontrar
    }

    public String findTipoPercurso(){
        List<Page> listaPaginas = mWizardModel.getCurrentPageSequence();
        for (Page p : listaPaginas)
            if (p.getKey().toString().contains(getResources().getString(R.string.paginaTipoPercurso)))
                return p.getData().toString();

        return null; // retorna nulo se não encontrar
    }

    public String findPontoPartida(){

        List<Page> listaPaginas = mWizardModel.getCurrentPageSequence();
        for (Page p : listaPaginas)
            if (p.getKey().toString().contains(getResources().getString(R.string.paginaPontoPartida)))
                return p.getData().toString();

        return null; // retorna nulo se não encontrar
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWizardModel  = new Wizard(MainActivity.this);

        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mWizardModel.load(savedInstanceState.getBundle("model"));
        }

        mWizardModel.registerListener(this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mStepPagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
            @Override
            public void onPageStripSelected(int position) {
                position = Math.min(mPagerAdapter.getCount() - 1, position);
                if (mPager.getCurrentItem() != position) {
                    mPager.setCurrentItem(position);
                }
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);

                if (mConsumePageSelectedEvent) {
                    mConsumePageSelectedEvent = false;
                    return;
                }

                mEditingAfterReview = false;
                updateBottomBar();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPager.getCurrentItem() == mCurrentPageSequence.size()) {
                    iniciarNavegacao();

                } else {
                    if (mEditingAfterReview) {
                        mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
                    } else {
                        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                    }
                }
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        });

        onPageTreeChanged();
        updateBottomBar();
    }

    @Override
    public void onPageTreeChanged() {
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        recalculateCutOffPage();
        mStepPagerStrip.setPageCount(mCurrentPageSequence.size() + 1); // + 1 = review step
        mPagerAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    private void updateBottomBar() {
        int position = mPager.getCurrentItem();
        if (position == mCurrentPageSequence.size()) {
            mNextButton.setText(R.string.finish);
            mNextButton.setBackgroundResource(R.drawable.finish_background);
            mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
        } else {
            mNextButton.setText(mEditingAfterReview
                    ? R.string.review
                    : R.string.next);
            mNextButton.setBackgroundResource(R.drawable.selectable_item_background);
            TypedValue v = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
            mNextButton.setTextAppearance(this, v.resourceId);
            mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
        }

        mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWizardModel.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("model", mWizardModel.save());
    }

    @Override
    public AbstractWizardModel onGetModel() {
        return mWizardModel;
    }

    @Override
    public void onEditScreenAfterReview(String key) {
        for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
            if (mCurrentPageSequence.get(i).getKey().equals(key)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                mPager.setCurrentItem(i);
                updateBottomBar();
                break;
            }
        }
    }

    @Override
    public void onPageDataChanged(Page page) {
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
        }
    }

    @Override
    public Page onGetPage(String key) {
        return mWizardModel.findByKey(key);
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size() + 1;
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }

    @Override
    public void onDialogPositiveClick(android.app.DialogFragment dialog) {
        Toast.makeText(this, "Positivo", Toast.LENGTH_SHORT).show();
//        mPage.getData().putString(Page.SIMPLE_DATA_KEY,
//                getListAdapter().getItem(position).toString());

    }

    @Override
    public void onDialogNegativeClick(android.app.DialogFragment dialog) {
        Toast.makeText(this, "Selecione um ponto no mapa", Toast.LENGTH_SHORT).show();
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private int mCutOffPage;
        private Fragment mPrimaryItem;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i >= mCurrentPageSequence.size()) {
                return new ReviewFragment();
            }

            return mCurrentPageSequence.get(i).createFragment();
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO: be smarter about this
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }

            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

        @Override
        public int getCount() {
            if (mCurrentPageSequence == null) {
                return 0;
            }
            return Math.min(mCutOffPage + 1, mCurrentPageSequence.size() + 1);
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }
    }

    public void determinarDistanciasProxPonto(ArrayList<Coordinate> lista){
        float dist = 0;
        int tamanho = lista.size();
        for (int i = 0; i < tamanho - 1; i++) {
            dist = PointsHandler.calcularDistancia(lista.get(i), lista.get(i+1));
            lista.get(i).setDistanciaProximoPonto(dist);
        }
        lista.get(tamanho-1).setDistanciaProximoPonto(0);
    }


    public void iniciarNavegacao(){
        setNomePropriedade(findNomePropriedade()); //percorre as paginas em busca do nomePropriedade
        setTipoPercurso(findTipoPercurso());       //percorre as paginas em busca do tipoPercurso
        setPontoPartida(findPontoPartida());       //percorre as paginas em busca do pontoPartida

        // Retirar os caracteres adicionais, espaços em branco e adiciona a extensão XML
        setNomePropriedade(getNomePropriedade().replace("Bundle[{_=", "")
                .replace("}]", ""));
        setNomePropriedade(getNomePropriedade().replace(" ", ""));
        setNomePropriedade(getNomePropriedade().concat(".xml"));

        // Retirar os caracteres adicionais
        setTipoPercurso(getTipoPercurso().replace("Bundle[{_=", "").replace("}]", ""));

        // Se for o percurso padrão, inicia pelo ponto 1
        if (getTipoPercurso().equals(getResources().getString(R.string.percursoPadrao)))
            setPontoPartida("Ponto 1");
        else
            setPontoPartida(getPontoPartida().replace("Bundle[{_=", "").replace("}]", ""));

        ArrayList<Coordinate> pointsList = new ArrayList<>();
        ListPoints lpff = new ListPointsFromFile(this);
        pointsList = lpff.getPointsList(getNomePropriedade());

        if (getTipoPercurso().equals(getResources().getString(R.string.percursoPadrao))){
            determinarDistanciasProxPonto(pointsList);

            Intent intent = new Intent(getBaseContext(), Navigator.class);
            intent.putExtra("Points", pointsList);
            startActivity(intent);
        } else{
            String nomePontoPartida = getPontoPartida();
            Coordinate coordinatePontoPartida = null;
            for (Coordinate c: pointsList) {
                String s = "Ponto ".concat(c.getDescricao());
                if (nomePontoPartida.equals(s)){
                    coordinatePontoPartida = c;
                    break;
                }
            }
            ArrayList<Coordinate> pointsListOrdinated = PointsHandler.orderPoints(coordinatePontoPartida, pointsList);
            determinarDistanciasProxPonto(pointsListOrdinated);

            Intent intent = new Intent(getBaseContext(), Navigator.class);
            intent.putExtra("Points", pointsListOrdinated);
            startActivity(intent);
        }
    }

}
