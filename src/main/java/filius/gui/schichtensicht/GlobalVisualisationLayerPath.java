package filius.gui.schichtensicht;

import filius.rahmenprogramm.I18n;

public class GlobalVisualisationLayerPath extends VisualisationLayerPath implements I18n {
	
	public GlobalVisualisationLayerPath(PanelLayerPath panelLayerPath) {
		super(panelLayerPath);
		backAndForth();
	}

//	@Override
//	public Dimension getPreferredSize() {
//		return new Dimension(480, 300);
//	}
//
//	@Override
//	public Dimension getMinimumSize() {
//		return new Dimension(480, 300);
//	}
//
//	@Override
//	public Dimension getMaximumSize() {
//		return new Dimension(480, 300);
//	}

	@Override
	protected void updateButtonsIfForth() {
	}

	@Override
	protected void updateButtonsIfBack() {
	}

	@Override
	protected void hideOrShowLayersIfForth() {
	}

	@Override
	protected void hideOrShowLayersIfBack() {
	}

	@Override
	protected void updateActiveBorder(boolean isForth) {
	}
}