package br.eti.x87.wizard;

import android.content.Context;

import java.util.List;

import br.eti.x87.property.ListProperties;
import br.eti.x87.property.ListPropertiesFromFile;
import br.eti.x87.snac.R;
import br.eti.x87.wizard.model.AbstractWizardModel;
import br.eti.x87.wizard.model.BranchPage;
import br.eti.x87.wizard.model.Instruction4PercursoPage;
import br.eti.x87.wizard.model.Instruction4PercursoPersonalizadoPage;
import br.eti.x87.wizard.model.Instruction4PropertyPage;
import br.eti.x87.wizard.model.MapPage;
import br.eti.x87.wizard.model.PageList;
import br.eti.x87.wizard.model.WelcomePage;

public class Wizard extends AbstractWizardModel {

    public Wizard(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        ListProperties lpp = new ListPropertiesFromFile(mContext);
        List<String> minhaLista = lpp.getPropertiesListArray();
        int tamanhoListaPropriedades = minhaLista.size();

        PageList pages = new PageList();
        pages.add(new WelcomePage(this, "Bem vindo"));
        pages.add(new Instruction4PropertyPage(this, ":: Propriedade"));

        BranchPage branchPage = new BranchPage(this,
                mContext.getResources().getString(R.string.paginaPropriedade));
        for (int i = 0; i < tamanhoListaPropriedades; i++) {
            String nomePropriedade = minhaLista.get(i);
            branchPage.addBranch(nomePropriedade,
                    new Instruction4PercursoPage(this, ":: Percurso"),
                    new BranchPage(this, mContext.getResources().getString(R.string.paginaTipoPercurso))
                            .addBranch(mContext.getResources().getString(R.string.percursoPadrao))
                            .addBranch(mContext.getResources().getString(R.string.percursoPersonalizado),
                                    new Instruction4PercursoPersonalizadoPage(this, ":: Percurso Personalizado"),
                                    new MapPage(this, mContext.getResources().getString(R.string.paginaPontoPartida), i, mContext).setRequired(true)
                            ).setRequired(true)
            ).setRequired(true);
        }
        pages.add(branchPage);
        return pages;
    }
}