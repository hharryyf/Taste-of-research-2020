package qbfsolver;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		final ExecutorService service = Executors.newSingleThreadExecutor();
		
        try {
                final Future<Object> f = service.submit(() -> {
            	QdimacFileReader rd = new QdimacFileReader();
        		CnfExpression fo = rd.read();
        		CmdArgs arg = ResultGenerator.getCommandLine();
        		Solver s = new PNSv2();
        		if (args.length >= 1) {
        			if (args[0].charAt(0) == '0') {
        				System.out.println("using brute force");
        				s = new BruteForce();
        			} else if (args[0].charAt(0) == '1') {
        				System.out.println("using PNS return to the root with standard intialization");
        				s = new PNS();
        				arg.setType(1);
        			} else if (args[0].charAt(0) == '2') {
        				System.out.println("using PNS with stack with standard intialization");
        				s = new PNSv2();
        				arg.setType(2);
        			} else if (args[0].charAt(0) == '3') {
        				System.out.println("using standard PNS with mobility intialization");
        				s = new PNS();
        				arg.setType(3);
        			}
        		} else {
        			System.out.println("using PNS with stack with mobility intialization");
        			arg.setType(0);
        		}
        		boolean res = s.solve(fo);
        		if (s.getClass() == BruteForce.class) {
        			Result ret = ResultGenerator.getInstance();
        			ret.setTruth(res);
        			ret.setIteration(0);
        		}
        		
        		System.out.println(res);
        		return ResultGenerator.getInstance();
            });

            System.out.println(f.get(900, TimeUnit.SECONDS));
            
        } catch (final TimeoutException e) {
            System.out.println("UNSOLVED NA");
            service.shutdown();
            System.exit(0);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
        	service.shutdown();
        }

	}

}
