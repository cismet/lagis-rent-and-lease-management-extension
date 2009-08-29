/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.lagis.ressort.mipa;

import de.cismet.lagisEE.entity.extension.vermietung.MiPaMerkmal;
import javax.swing.JCheckBox;

/**
 *
 * @author Sebastian Puhl
 */
public class MerkmalCheckBox extends JCheckBox {

    private MiPaMerkmal miPaMerkmal;

    public MerkmalCheckBox(MiPaMerkmal miPa) {
        super(miPa.getBezeichnung());
        this.miPaMerkmal = miPa;
    }

    public MiPaMerkmal getMiPaMerkmal() {
        return miPaMerkmal;
    }
                
}
