/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.lagis.ressort.mipa;

import de.cismet.cids.custom.beans.lagis.MipaCustomBean;

import de.cismet.lagis.gui.tables.AbstractCidsBeanTable_Lagis;
import de.cismet.lagis.gui.tables.RemoveActionHelper;

/**
 * DOCUMENT ME!
 *
 * @author   gbaatz
 * @version  $Revision$, $Date$
 */
public class MipaTable extends AbstractCidsBeanTable_Lagis {

    //~ Instance fields --------------------------------------------------------

    private RemoveActionHelper removeActionHelper;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RemoveActionHelper getRemoveActionHelper() {
        return removeActionHelper;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  removeActionHelper  DOCUMENT ME!
     */
    public void setRemoveActionHelper(final RemoveActionHelper removeActionHelper) {
        this.removeActionHelper = removeActionHelper;
    }

    @Override
    protected void addNewItem() {
        final MipaCustomBean tmpMiPa = MipaCustomBean.createNew();
        ((MiPaModel)getModel()).addCidsBean(tmpMiPa);
    }

    @Override
    protected void removeItem(final int row) {
        ((MiPaModel)getModel()).removeCidsBean((this.getFilters().convertRowIndexToModel(getSelectedRow())));
        removeActionHelper.duringRemoveAction(this);
    }
}
