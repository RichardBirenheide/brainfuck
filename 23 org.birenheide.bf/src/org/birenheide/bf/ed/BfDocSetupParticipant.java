package org.birenheide.bf.ed;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

public class BfDocSetupParticipant implements IDocumentSetupParticipant {
	
	public static final String BF_PARTITIONING = "BrainFuck_Partitioning";

	@Override
	public void setup(IDocument document) {
		
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 ext3 = (IDocumentExtension3) document;
			
			IDocumentPartitioner partitioner = new FastPartitioner(null, null);
			ext3.setDocumentPartitioner(BF_PARTITIONING, partitioner);
		}
	}
}
