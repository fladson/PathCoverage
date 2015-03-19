import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import br.ufrn.dimap.plea.flowanalyzer.callgraph.ChainNode;
import br.ufrn.dimap.plea.flowanalyzer.wala.util.AnalysisScopeException;
import br.ufrn.dimap.plea.flowanalyzer.wala.util.HierarchyConstructionException;
import br.ufrn.dimap.plea.flowanalyzer.wala.util.Loader;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.strings.Atom;


public class CallGraphWALA {
	
	private CallGraph cg = null;	
	private Loader loader;	
	private List<ChainNode> handlerChainNodes = null;	
	
	
	// retorna os entrypoints que correspondem a uma library
		public Iterable<Entrypoint> makeLibraryEntrypoints(AnalysisScope scope, IClassHierarchy cha){		
			if(cha == null){
				throw new IllegalArgumentException("cha is null");
			}					
			
			final HashSet<Entrypoint> entryPoints = HashSetFactory.make();						
							
			for (IClass klass : cha) {															
				if (!scope.isApplicationLoader(klass.getClassLoader())) continue;			
				
				for (IMethod m : klass.getDeclaredMethods()) {
					if (m.isPublic() || m.isProtected()) { // this method is public or protected															
						entryPoints.add(new DefaultEntrypoint(m, cha));
					}
				}											
			}		
			return entryPoints;
		}
	
	//carrega o callgraph usando as configurações do arquivo associado a WALA.
		public void init(boolean param, String application, String libs) throws UnsupportedOperationException, InvalidClassFileException {		
			if(cg == null){
				try {
					//configurações do projeto serão carregadas pelo loader.
					this.loader = new Loader(param, application, libs);
					
					// path principal do projeto
					String path = application.substring(0, application.lastIndexOf('\\')+1);
					
					
					// carregando a hierarquia de classes e exibindo o escopo.				 			
					this.loader.loadAnalysisScope();
					this.loader.loadClassHierarchy();
					
					// inicializando a lista de chain nodes.
					handlerChainNodes = new ArrayList<ChainNode>();

					// insere o main como entryPoint.
					Iterable<Entrypoint> entrypoints = null; 				
					if(this.loader.isJavaApp()) // Java
						entrypoints = Util.makeMainEntrypoints(loader.getAnalysisScope(), loader.getClassHierarchy());
//					else if(this.loader.isAndroidApp()){ // Android					
//						EntryPointsUtil.makeAndroidApplicationEntrypoints(path, loader.getAnalysisScope(), loader.getClassHierarchy());
//						entrypoints = EntryPointsUtil.getEntries();
//					} else if(this.loader.isSigaaProject()) // Sigaa				
//						entrypoints = this.makeSigaaLibraryEntrypoints(loader.getAnalysisScope(), loader.getClassHierarchy());
					else if(this.loader.isLibrary()) // Library
						entrypoints = this.makeLibraryEntrypoints(loader.getAnalysisScope(), loader.getClassHierarchy());
																
					
					// DEBUG
//					if(Main.DEBUG_MODE) {
						System.out.println((new Date()).toString()+" - EntryPoints created.");							
						System.out.println("============= Entry points ================");
						Iterator<Entrypoint> it_entrypoints = entrypoints.iterator(); 
						while(it_entrypoints.hasNext()){
							System.out.println(it_entrypoints.next());
						}
						System.out.println("===========================================");
//					}				
					
					
					AnalysisOptions options = new AnalysisOptions(loader.getAnalysisScope(), entrypoints);	
					AnalysisCache cache = new AnalysisCache();				

					/**
					 * mais informações: http://wala.sourceforge.net/wiki/index.php/UserGuide:PointerAnalysis#Improving_Scalability
					 */
					options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);									

					SSAPropagationCallGraphBuilder builder = null;

//					if(this.loader.isZeroCFA()){ // configuração da análise 0-CFA.
						IClassHierarchy cha = this.loader.getClassHierarchy();
						AnalysisScope scope = this.loader.getAnalysisScope();

						builder = Util.makeZeroOneCFABuilder(options, cache, cha, scope, null, null);					
						this.cg = builder.makeCallGraph(options,null);
						System.out.println((new Date()).toString()+" - CallGraph created.");
						System.out.println(CallGraphStats.getStats(cg));
						
						// imprime todas as ramificações a partir dos entries points até um cgnode primordial.
					    Collection<CGNode> entries = cg.getEntrypointNodes();	    
						for ( Iterator<CGNode> i = entries.iterator() ; i.hasNext() ;)
						{			
							CGNode entrypoint = i.next();			
							printCallGraphNode(cg, entrypoint, 0);
						}	
						

//					} else if (this.loader.iskCFA()){ // configuração da análise kCFA.
//						IClassHierarchy cha = this.loader.getClassHierarchy();
//						AnalysisScope scope = this.loader.getAnalysisScope();
//
//						Util.addDefaultSelectors(options, cha);
//						Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);
//
//						// builder com sensibilidade ao contexto na sinalização de exceções.
//						builder = new nCFABuilder(1,cha,options,cache,null,null);
//						
//						this.cg = builder.makeCallGraph(options,null);									
//					}																																											

				} catch (AnalysisScopeException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (HierarchyConstructionException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (CallGraphBuilderCancelException e) {
					e.printStackTrace();
				}						
			}
		}
		
		private void printCallGraphNode(CallGraph cg, CGNode currNode, int level) {		
			
			if(getAtomLoaderReference(currNode) != AnalysisScope.PRIMORDIAL) {		
				printLevelTabs(level);
				System.out.println (currNode.getMethod().getSignature());
				for( Iterator<CGNode> preds = cg.getPredNodes(currNode); preds.hasNext() ;)
				{
					printCallGraphNode(cg,preds.next(),level+1);
				}										
			}
		}
		
		private void printLevelTabs(int level) {
			for(int i=0;i<level;i++)
				System.out.print("\t");		
		}
		
		private Atom getAtomLoaderReference(CGNode node) {
			return node.getMethod().getReference().getDeclaringClass().getClassLoader().getName();
		}
		
}
