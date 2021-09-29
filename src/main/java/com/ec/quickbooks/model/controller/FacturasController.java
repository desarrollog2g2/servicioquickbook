package com.ec.quickbooks.model.controller;

import com.ec.quickbooks.model.helper.QBOServiceHelper;
import com.ec.quickbooks.model.service.OAuth2PlatformClientFactory;
import com.intuit.ipp.data.Error;
import com.intuit.ipp.data.*;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @author dderose
 *
 */
@Controller
public class FacturasController {

    @Autowired
    OAuth2PlatformClientFactory factory;

    @Autowired
    public QBOServiceHelper helper;


    private static final Logger logger = Logger.getLogger(CompanyInfoController.class);
    private static final String failureMsg="Failed";
    private HttpSession sesion = null;

    /**
     * Sample QBO API call using OAuth2 tokens
     *
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/getFacturas")
    public String callFacturasInfo(HttpSession session) {
        sesion = session;
        String realmId = (String)session.getAttribute("realmId");
        if (StringUtils.isEmpty(realmId)) {
            return new JSONObject().put("response","No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
        }
        String accessToken = (String)session.getAttribute("access_token");

        try {


            //Dataservice
            DataService service = helper.getDataService(realmId, accessToken);

            // get all Facturas
            String sql = "select * from invoice";
            QueryResult queryResult = service.executeQuery(sql);
            //return processResponse(failureMsg, queryResult);
            return null;

        }
        /*
         * Manejo de excepcion error de token
         */
        catch (InvalidTokenException e) {
            logger.error("Error while calling executeQuery :: " + e.getMessage());

            //refresco de tokens
            logger.info("Se recivio 401 durinante la llamada, refrescando los tokens ahora");
            OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
            String refreshToken = (String)session.getAttribute("refresh_token");

            try {
                BearerTokenResponse bearerTokenResponse = client.refreshToken(refreshToken);
                session.setAttribute("access_token", bearerTokenResponse.getAccessToken());
                session.setAttribute("refresh_token", bearerTokenResponse.getRefreshToken());

                //call company info again using new tokens
                logger.info("llamando informacion de facturas con tojkens refrescados");
                DataService service = helper.getDataService(realmId, accessToken);

                // get all companyinfo
                String sql = "select * from invoice";
                QueryResult queryResult = service.executeQuery(sql);
                //return processResponse(failureMsg, queryResult);
                return null;

            } catch (OAuthException e1) {
                logger.error("Error while calling bearer token :: " + e.getMessage());
                return new JSONObject().put("response",failureMsg).toString();
            } catch (FMSException e1) {
                logger.error("Error while calling company currency :: " + e.getMessage());
                return new JSONObject().put("response",failureMsg).toString();
            }

        } catch (FMSException e) {
            List<Error> list = e.getErrorList();
            list.forEach(error -> logger.error("Error while calling executeQuery :: " + error.getMessage()));
            return new JSONObject().put("response",failureMsg).toString();
        }


    }

    private Customer getFacturaCostumer(String value){
        String realmId = (String)sesion.getAttribute("realmId");
        if (StringUtils.isEmpty(realmId)) {
            logger.info("No realm ID.  QBO calls only work if the accounting scope was passed!");
            return null;
        }
        String accessToken = (String)sesion.getAttribute("access_token");

        try {


            //Dataservice
            DataService service = helper.getDataService(realmId, accessToken);

            // get all Facturas
            String sql = "select * from customer where id = '"+value+"'";
            QueryResult queryResult = service.executeQuery(sql);
            Customer cliente  = (Customer) queryResult.getEntities().get(0);
            return cliente;
        }
        /*
         * Manejo de excepcion error de token
         */
        catch (InvalidTokenException e) {
            logger.error("Error while calling executeQuery :: " + e.getMessage());

            //refresco de tokens
            logger.info("Se recivio 401 durinante la llamada, refrescando los tokens ahora");
            OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
            String refreshToken = (String)sesion.getAttribute("refresh_token");

            try {
                BearerTokenResponse bearerTokenResponse = client.refreshToken(refreshToken);
                sesion.setAttribute("access_token", bearerTokenResponse.getAccessToken());
                sesion.setAttribute("refresh_token", bearerTokenResponse.getRefreshToken());

                //call company info again using new tokens
                logger.info("llamando informacion de facturas con tojkens refrescados");
                DataService service = helper.getDataService(realmId, accessToken);

                // get all companyinfo
                String sql = "select * from customer where id = "+value;
                QueryResult queryResult = service.executeQuery(sql);
                Customer cliente  = (Customer) queryResult.getEntities().get(0);
                return cliente;

            } catch (OAuthException e1) {
                logger.error("Error while calling bearer token :: " + e.getMessage());
                return null;
            } catch (FMSException e1) {
                logger.error("Error while calling company currency :: " + e.getMessage());
                return null;
            }

        } catch (FMSException e) {
            List<Error> list = e.getErrorList();
            list.forEach(error -> logger.error("Error while calling executeQuery :: " + error.getMessage()));
            return null;

        }

    }

    /*
    private String processResponse(String failureMsg, QueryResult queryResult) {
        int i= 0;
        String numeroFact= "";
        CompanyInfo company= getTraerCompania();
        company.getEmployerId();
        List<Empresa> listEmpresas= (List<Empresa>) empresaRepository.findAll();
        Empresa empresa= new Empresa();

        if(!listEmpresas.isEmpty() && company.getEmployerId()!=null){
            for(Empresa emp: listEmpresas){
                if(emp.getNumIdent().trim().equals(company.getEmployerId().trim())){
                    empresa= emp;
                }else{
                    empresa= null;
                }
            }
        }else{
            empresa= null;
        }


        if(empresa==null){
            empresa= new Empresa();
            empresa.setCiudad(company.getCompanyAddr().getCountrySubDivisionCode());
            empresa.setNombreEmpresa(company.getLegalName());
            empresa.setDireccion(company.getCompanyAddr().getLine1());
            empresa.setPais(company.getLegalAddr().getCountry());
            empresa.setNumIdent(company.getEmployerId()==null?"0000000000001":company.getEmployerId());
            empresa.setEmail(company.getEmail().getAddress());
            empresa.setCodigoPostal(company.getLegalAddr().getPostalCode());
            empresa.setFechaCreacion(new Date());
            empresa.setCodigoFactura(0);
            empresaRepository.save(empresa);



            if(empresa.getIdEmpresa().toString().length()==1){
                empresa.setPuntoFact("001-00" + empresa.getIdEmpresa());
            }else if(empresa.getIdEmpresa().toString().length()==2){
                empresa.setPuntoFact("001-0" + empresa.getIdEmpresa());
            }else if(empresa.getIdEmpresa().toString().length()>=3){
                empresa.setPuntoFact("001-" + empresa.getIdEmpresa());
            }

            empresaRepository.save(empresa);
        }




        if (!queryResult.getEntities().isEmpty() && queryResult.getEntities().size() > 0) {
            List<Invoice> facturas  = (List<Invoice>) queryResult.getEntities();
            for(Invoice invoice : facturas) {
                ReferenceType clienteRef = invoice.getCustomerRef();
                Customer cliente = getFacturaCostumer(clienteRef.getValue());
                ReferenceType identificadorImpuesto = cliente.getDefaultTaxCodeRef();
                List<Line> detallesFactura = invoice.getLine();

                ObjectMapper mapper = new ObjectMapper();
                FacturaRepository facturaDAO;
                Factura factura = new Factura();
                factura.setFechafacF(new Timestamp(invoice.getTxnDate().getTime()));
                factura.setIdPuntoFacturacion("001-001");
                factura.setDescuentoF(invoice.getDiscountRate() != null ? invoice.getDiscountRate().doubleValue() : 0);
                factura.setIvaF(invoice.getTxnTaxDetail().getTotalTax().doubleValue());
                factura.setTotalF(invoice.getTotalAmt().doubleValue());
                factura.setSubtotalF(factura.getTotalF()- factura.getIvaF());
                factura.setCedulaCli(cliente.getPrimaryTaxIdentifier().contains("X")?cliente.getAlternatePhone().getFreeFormNumber():cliente.getPrimaryTaxIdentifier());
                factura.setRucCli(cliente.getPrimaryTaxIdentifier().contains("X")?cliente.getAlternatePhone().getFreeFormNumber():cliente.getPrimaryTaxIdentifier());
                factura.setNombreCli(cliente.getFullyQualifiedName());
                factura.setDireccionCli((cliente.getBillAddr().getLine1()==null?"":(cliente.getBillAddr().getLine1() + " "))  + cliente.getBillAddr().getCity()==null?"":cliente.getBillAddr().getCity());
                factura.setEmailCl(cliente.getPrimaryEmailAddr().getAddress());
                factura.setIdFactQuick(invoice.getId());
                factura.setIdEmpresa(empresa.getIdEmpresa());
                factura.setIdEstadoFactura("1");
                factura.setIdFormaPago("1");


                factura.setIdFactura(this.getGenerarIdFactura(empresa));
                facturaRepository.save(factura);
                empresa.setCodigoFactura(empresa.getCodigoFactura() + 1);
                empresaRepository. save(empresa);
                for(Line linea : detallesFactura){
                    Detallefactura detallefactura = new Detallefactura();
                    //detallefactura.setIdDetallefactura("PR-01");
                    detallefactura.setIdFactura(factura.getIdFactura());
                    detallefactura.setPorcentajeiva(linea.getTaxLineDetail()!=null?linea.getTaxLineDetail().getTaxPercent(): BigDecimal.valueOf(0));
                   detallefactura.setCostoDf(linea.getAmount().doubleValue());


                   if(linea.getSalesItemLineDetail()!=null){
                       detallefactura.setIdConcepto(linea.getSalesItemLineDetail().getItemRef().getName() + " " + linea.getDescription());
                       detallefactura.setUnidadesDf(linea.getSalesItemLineDetail().getQty().intValue());
                       detallefactura.setCostotDf(detallefactura.getUnidadesDf() * detallefactura.getCostoDf());
                       detallefactura.setSubtotal(detallefactura.getCostotDf());

                       BigDecimal bd = new BigDecimal(0);
                      Double ivaVal = (double) Math.round((12) * 100);
                       ivaVal = ivaVal / 100;
                       bd = new BigDecimal(ivaVal);

                       detallefactura.setPorcentajeiva(bd.setScale(2, RoundingMode.HALF_UP));
                       detallefactura.setValorIva(linea.getTaxLineDetail()!=null?linea.getTaxLineDetail().getNetAmountTaxable().doubleValue(): 0);
                       detallefacturaRepository.save(detallefactura);
                   }

                }



                ///GENERAR REGISTRO DE PAGOS
                BigDecimal bd = new BigDecimal(0);
                Double valTotal=(double) Math.round((factura.getTotalF()) * 100);
                valTotal= valTotal/100;
                bd = new BigDecimal(valTotal);

                Pagos pagos= new Pagos();
                pagos.setIdFactura(factura.getIdFactura());
                pagos.setMontoPa(bd.setScale(2, RoundingMode.HALF_UP));
                pagos.setIdformaPago("1");
                pagosRepository.save(pagos);

                i=i+1;
               numeroFact =  "FacturasiIngresadas al sistema" +  String.valueOf(i);
                try {
                    String jsonInString = mapper.writeValueAsString(facturas);
                    String mensaje= "Facturas ingresadas correctamente";
                    return jsonInString;
                } catch (JsonProcessingException e) {
                    logger.error("Exception al obtener la informacion de factura ", e);
                    return new JSONObject().put("response", failureMsg).toString();
                }
            }
        }
        return failureMsg;
    }


    private String getGenerarIdFactura(Empresa empresa){
        String codigoFactura= "";
        try {
            if(empresa.getCodigoFactura()==0){
                codigoFactura=  empresa.getPuntoFact()  + "-000000001";
            }else {
                String puntoFact= empresa.getPuntoFact()  + "-";
                if (empresa.getCodigoFactura().toString().length() == 1) {
                    codigoFactura = puntoFact + "00000000" + empresa.getCodigoFactura();
                }
                if (empresa.getCodigoFactura().toString().length() == 2) {
                    codigoFactura = puntoFact + "0000000" + empresa.getCodigoFactura();
                }
                if (empresa.getCodigoFactura().toString().length() == 3) {
                    codigoFactura = puntoFact + "000000" + empresa.getCodigoFactura();
                }
                if (empresa.getCodigoFactura().toString().length() == 4) {
                    codigoFactura = puntoFact +"00000" + empresa.getCodigoFactura();
                }
                if (empresa.getCodigoFactura().toString().length() == 5) {
                    codigoFactura = puntoFact + "0000" + empresa.getCodigoFactura();
                }
                if (empresa.getCodigoFactura().toString().length() == 6) {
                    codigoFactura = puntoFact + "000" + empresa.getCodigoFactura();
                }
                if (empresa.getCodigoFactura().toString().length() == 7) {
                    codigoFactura = puntoFact + "00" + empresa.getCodigoFactura();
                }
                if (empresa.getCodigoFactura().toString().length() == 8) {
                    codigoFactura = puntoFact + "0" + empresa.getCodigoFactura();
                }


            }
            return codigoFactura;
        }catch (Exception e){
            return"";
        }

    }
*/

    private CompanyInfo getTraerCompania(){
        String realmId = (String)sesion.getAttribute("realmId");
        if (StringUtils.isEmpty(realmId)) {
            logger.info("No realm ID.  QBO calls only work if the accounting scope was passed!");
            return null;
        }
        String accessToken = (String)sesion.getAttribute("access_token");

        try {


            //Dataservice
            DataService service = helper.getDataService(realmId, accessToken);

            // get all Facturas
            String sql = "select * from companyinfo";
            QueryResult queryResult = service.executeQuery(sql);
            CompanyInfo company  = (CompanyInfo) queryResult.getEntities().get(0);
            return company;
        }
        /*
         * Manejo de excepcion error de token
         */
        catch (InvalidTokenException e) {
            logger.error("Error while calling executeQuery :: " + e.getMessage());

            //refresco de tokens
            logger.info("Se recivio 401 durinante la llamada, refrescando los tokens ahora");
            OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
            String refreshToken = (String)sesion.getAttribute("refresh_token");

            try {
                BearerTokenResponse bearerTokenResponse = client.refreshToken(refreshToken);
                sesion.setAttribute("access_token", bearerTokenResponse.getAccessToken());
                sesion.setAttribute("refresh_token", bearerTokenResponse.getRefreshToken());

                //call company info again using new tokens
                logger.info("llamando informacion de facturas con tojkens refrescados");
                DataService service = helper.getDataService(realmId, accessToken);

                // get all companyinfo
                String sql = "select * from companyinfo";
                QueryResult queryResult = service.executeQuery(sql);
                CompanyInfo company  = (CompanyInfo) queryResult.getEntities().get(0);
                return company;

            } catch (OAuthException e1) {
                logger.error("Error while calling bearer token :: " + e.getMessage());
                return null;
            } catch (FMSException e1) {
                logger.error("Error while calling company currency :: " + e.getMessage());
                return null;
            }

        } catch (FMSException e) {
            List<Error> list = e.getErrorList();
            list.forEach(error -> logger.error("Error while calling executeQuery :: " + error.getMessage()));
            return null;

        }
    }

}
