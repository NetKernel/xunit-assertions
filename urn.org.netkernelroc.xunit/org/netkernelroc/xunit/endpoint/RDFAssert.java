package org.netkernelroc.xunit.endpoint;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponseReadOnly;
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
    Boolean result = Boolean.FALSE;
    INKFResponseReadOnly response = context.source("arg:response", INKFResponseReadOnly.class);
    String rdf = context.source("arg:result", String.class);
    String tagValue = context.source("arg:tagValue", String.class);
    String testName = context.source("arg:testName", String.class);

    String mimeType = response.getMimeType();

    System.out.println(rdf);
    System.out.println(mimeType);
    System.out.println(tagValue);
    System.out.println(testName);

    IReadableBinaryStreamRepresentation irbsr = context.source("arg:result", IReadableBinaryStreamRepresentation.class);
    InputStream is = irbsr.getInputStream();

    //RIOT.init();

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

    // Determine the requested test
    if ("TripleCount".equals(testName))
      {
      int testCount = Integer.parseInt(tagValue);
      StmtIterator iter = model.listStatements();
      int statementCounter = 0;
      while (iter.hasNext() )
        {
        Statement statement = iter.next();
        System.out.println("[" + statement.getSubject() + "] [" + statement.getPredicate() + "] [" +statement.getObject() + "]");
        statementCounter = statementCounter + 1;
        }

      System.out.println("There are [" + statementCounter + "] statements.");
      result = statementCounter == testCount ? Boolean.TRUE : Boolean.FALSE;
      }

//    IHDSNode assertions = context.source("arg:testValue", IHDSNode.class);

    context.createResponseFrom(result);
    }
  }
