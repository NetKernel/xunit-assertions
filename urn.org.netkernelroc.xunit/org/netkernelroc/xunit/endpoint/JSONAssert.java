package org.netkernelroc.xunit.endpoint;

import org.json.JSONObject;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

public class JSONAssert extends StandardAccessorImpl
  {

  public JSONAssert()
    {
    declareThreadSafe();
    }


  public void onSource(INKFRequestContext context) throws Exception
    {
    StringBuilder expected = new StringBuilder();
    StringBuilder received = new StringBuilder();

    Boolean result = Boolean.TRUE;

    IHDSNode assertions = context.source("arg:test", IHDSNode.class);

    for (IHDSNode assertion : assertions.getNodes("/assertions/*"))
      {
      String test = assertion.getName();
      String value = (String) assertion.getValue();

      // Determine if this is a JSON structure
      if ("assertIsJSON".equals(test))
        {
        try
          {
          JSONObject json = context.source("arg:result", JSONObject.class);
          }
        catch (Exception e)
          {
          // Some sort of transreption error
          result = Boolean.FALSE;
          expected.append(" representation type is  JSON");
          received.append(" representation type is not JSON");
          }
        }
      }

    context.sink("active:assert/Expected", expected.toString());
    context.sink("active:assert/Received", received.toString());

    context.createResponseFrom(result).setNoCache();
    }
  }
