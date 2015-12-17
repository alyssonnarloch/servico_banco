package resource;

import hibernate.Util;
import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import model.Account;
import model.SingleMessage;
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
    public String test() {
        return "{oieeee : 1}";
        
    }

    @GET
    @Path("/balanceverification/account/{account}/agency/{agency}/price/{price}")
    @Produces("application/json; charset=UTF-8")
    public Response balanceVerification(@PathParam("account") int account,
            @PathParam("agency") int agency,
            @PathParam("price") double price) {

        SessionFactory sf = Util.getSessionFactory();
        Session s = sf.openSession();
        Transaction t = s.getTransaction();

        SingleMessage message = new SingleMessage();

        try {
            t.begin();

            String sql = "FROM Account WHERE account = :account AND agency = :agency";

            Query query = s.createQuery(sql);
            query.setInteger("account", account);
            query.setInteger("agency", agency);

            List<Account> result = query.list();

            if (result.size() == 0) {
                message.setCode(Account.INVALID_DATA);
                message.setMessage("Invalid data.");
            } else {
                Account accountObj = result.get(0);

                if (accountObj.getBalance() >= price) {
                    message.setCode(Account.OK);
                    message.setMessage("Ok.");
                } else {
                    message.setCode(Account.BALANCE_NOT_ENOUGH);
                    message.setMessage("Balance not enough.");
                }
            }

            GenericEntity<SingleMessage> entity = new GenericEntity<SingleMessage>(message) {
            };

            t.commit();

            s.flush();
            s.close();

            return Response.ok(entity).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    @PUT
    @Path("/balanceupdate")
    @Produces("application/json; charset=UTF-8")
    public Response balanceUpdate(@FormParam("account") int account,
            @FormParam("agency") int agency,
            @FormParam("price") double price,
            @FormParam("operation") int operation) {

        SessionFactory sf = Util.getSessionFactory();
        Session s = sf.openSession();
        Transaction t = s.getTransaction();

        SingleMessage message = new SingleMessage();

        try {
            t.begin();

            String sql = "FROM Account WHERE account = :account AND agency = :agency";

            Query query = s.createQuery(sql);
            query.setInteger("account", account);
            query.setInteger("agency", agency);            

            List<Account> result = query.list();           
            
            if (!result.isEmpty()) {
                Account accountObj = (Account) query.list().get(0);
                
                double newBalance = 0.0;

                if (operation == Account.DEBT) {
                    newBalance = accountObj.getBalance() - price;
                } else if (operation == Account.CREDIT) {
                    newBalance = accountObj.getBalance() + price;
                }
                accountObj.setBalance(newBalance);

                s.update(accountObj);

                message.setCode(Account.OK);
                message.setMessage("Ok.");
            } else {
                message.setCode(Account.INVALID_DATA);
                message.setMessage("Invalid data.");
            }

            GenericEntity<SingleMessage> entity = new GenericEntity<SingleMessage>(message) {
            };

            t.commit();

            s.flush();
            s.close();

            return Response.ok(entity).build();
        } catch (Exception ex) {
            t.rollback();
            ex.printStackTrace();
            return Response.serverError().build();
        }

    }
}
