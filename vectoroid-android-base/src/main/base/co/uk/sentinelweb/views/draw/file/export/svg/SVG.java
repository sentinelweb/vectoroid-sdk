package co.uk.sentinelweb.views.draw.file.export.svg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;

import co.uk.sentinelweb.views.draw.file.SaveFile;
import co.uk.sentinelweb.views.draw.file.SaveFile.Option;
import co.uk.sentinelweb.views.draw.model.Drawing;

public class SVG {
	public static boolean saveSVG(final SaveFile s, final Drawing d, final ArrayList<Option> options) {
		try {
			s._options=options;
			//s.makeAssetList(d);
			final File file = s.getSVGFile(d.getId());
			final FileWriter out = new FileWriter(file);
			final BufferedWriter outs = new BufferedWriter(out,4096);
			final SVGDrawing svgd = new SVGDrawing(s);
			//StringBuffer outs = new StringBuffer();
			svgd.toSVG(d, outs);
			//out.write(outs.toString());
			outs.flush();
			out.flush();// shouldnt need this
			out.close();
			return true;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
}
