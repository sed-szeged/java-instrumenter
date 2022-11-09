package hu.szte.sed.coverage;

import hu.szte.sed.graph.data.Node;
import hu.szte.sed.util.Constants;

import java.io.File;

public class ChainCoverageData<T extends Number> implements CoverageData<T> {

	private Node<T> root;
	private Node<T> current;
	private boolean returning = false;

	@SuppressWarnings("unchecked")
	public ChainCoverageData() {
		root = current = new Node<T>((T) Constants.ROOT_ID, null);
	}

	@Override
	public void enter(final T methodId) {
		returning = false;

		current = current.addChildIfNotExists(methodId);
	}

	@Override
	public void leave(final T methodId) {
		if (!current.getCodeElementId().equals(methodId)) {
			System.out.println(String.format("Strange return from '%d'", methodId));

			while (!current.getCodeElementId().equals(methodId) && current.getParent() != null) {
				current = current.getParent();
			}
		}

		if (!returning) {
			current.setFinal(true);
			current.increaseCount();
		}

		returning = true;

		current = current.getParent();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void reset() {
		root = current = new Node<T>((T) Constants.ROOT_ID, null);
	}

	@Override
	public void saveData(final File dataFile) {
//		try {
//			try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dataFile)))) {
//				writeNumber(dos, Constants.MAGIC);
//				dos.writeByte(Granularity.CHAIN.getId());
//
//				final Preorder<T> alg = new Preorder<>();
//				final ChainCollectorVisitor<T> visitor = new ChainCollectorVisitor<>(dos);
//
//				alg.run(root, visitor);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
