package org.netkernelroc.xunit.endpoint;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponseReadOnly;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import java.io.InputStream;

public class RDFAssert extends StandardAccessorImpl
  {

  public RDFAssert()
    {
    declareThreadSafe();
    }

  @Override
  public void onSource(INKFRequestContext context) throws Exception
    {
    StringBuilder expected = new StringBuilder();
    StringBuilder received = new StringBuilder();

    Boolean result = Boolean.TRUE;


    INKFResponseReadOnly response = context.source("arg:response", INKFResponseReadOnly.class);
    String mimeType = response.getMimeType();

    IReadableBinaryStreamRepresentation irbsr = context.source("arg:result", IReadableBinaryStreamRepresentation.class);
    InputStream is = irbsr.getInputStream();

    Model model = ModelFactory.createDefaultModel(); // creates an in-memory Jena Model
    // Read and parse the model based on the mimeType
    if (mimeType == null || "application/rdf+xml".equals(mimeType))
      {
      model.read(is, null, "RDF/XML"); // parses an InputStream assuming RDF in RDF XML format
      }
    else if ("text/turtle".equals(mimeType))
      {
      model.read(is, null, "TURTLE"); // parses an InputStream assuming RDF in Turtle format
      }


    IHDSNode assertions = context.source("arg:test", IHDSNode.class);

    for (IHDSNode assertion : assertions.getNodes("/assertions/*"))
      {
      String test = assertion.getName();
      String value = (String) assertion.getValue();

      // Determine the requested test
      if ("tripleCount".equals(test))
        {
        int testCount = Integer.parseInt(value);
        StmtIterator iter = model.listStatements();
        int statementCounter = 0;
        while (iter.hasNext())
          {
          Statement statement = iter.next();
          System.out.println("[" + statement.getSubject() + "] [" + statement.getPredicate() + "] [" + statement.getObject() + "]");
          statementCounter = statementCounter + 1;
          }
        if (testCount != statementCounter)
          {
          result = Boolean.FALSE;
          expected.append(" count=" + testCount);
          received.append(" count=" + statementCounter);
          }
        }
      }
    context.sink("active:assert/Expected", expected.toString());
    context.sink("active:assert/Received", received.toString());

    context.createResponseFrom(result).setNoCache();
    }
  }
