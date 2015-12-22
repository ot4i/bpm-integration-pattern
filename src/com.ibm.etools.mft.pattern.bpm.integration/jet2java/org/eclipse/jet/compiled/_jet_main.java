package org.eclipse.jet.compiled;

import org.eclipse.jet.JET2Context;
import org.eclipse.jet.JET2Template;
import org.eclipse.jet.JET2Writer;

public class _jet_main implements JET2Template {

    public _jet_main() {
        super();
    }

    private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$

    public void generate(final JET2Context context, final JET2Writer __out) {
        JET2Writer out = __out;
 com.ibm.etools.mft.pattern.bpm.integration.code.Generation.onGenerate(context, "com.ibm.etools.mft.pattern.bpm.integration", "Id1399ce74cc34c1511d6a745ab59", "Id1399ce74cc3f59ec4fd5f753f25", "pattern"); 
        out.write(NL);         
    }
}
