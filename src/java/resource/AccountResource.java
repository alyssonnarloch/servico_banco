package resource;

import hibernate.Util;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import model.Account;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

@Path("account")
public class AccountResource {

    @Context
    private UriInfo context;

    public AccountResource() {
    }

    @GET
    @Produces("application/json")
    public String test(){
        return "{oieeee : 1}";
    }
    
    @GET
    @Path("/balanceverification/account/{account}/agency/{agency}/price/{price}")
    @Produces("application/json")
    public Response balanceVerification(@PathParam("account") int account,
            @PathParam("agency") int agency,
            @PathParam("price") double price) {

        SessionFactory sf = Util.getSessionFactory();
        Session s = sf.openSession();
        Transaction t = s.getTransaction();

        int resultCode;

        try {
            t.begin();
            
            String sql = "FROM Account WHERE account = :account AND agency = :agency";

            Query query = s.createQuery(sql);
            query.setInteger("account", account);
            query.setInteger("agency", agency);

            List<Account> result = query.list();

            if (result.size() == 0) {
                resultCode = Account.INVALID_DATA;
            } else {
                Account accountObj = result.get(0);

                if (accountObj.getBalance() >= price) {
                    resultCode = Account.OK;
                } else {
                    resultCode = Account.BALANCE_NOT_ENOUGH;
                }
            }
            t.commit();

            s.flush();
            s.close();

            return Response.ok(resultCode).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.serverError().build();
        }

    }
}
