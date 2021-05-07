package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	Graph<Fermata, DefaultEdge> grafo ;
	
	Map<Fermata, Fermata> predecessore;

	public void creaGrafo() {
		this.grafo = new SimpleGraph<>(DefaultEdge.class) ;
		
		MetroDAO dao = new MetroDAO() ;
		List<Fermata> fermate = dao.getAllFermate() ;
		
//		for(Fermata f : fermate) {
//			this.grafo.addVertex(f) ;
//		}
		
		Graphs.addAllVertices(this.grafo, fermate) ;
		
		// Aggiungiamo gli archi
		
//		for(Fermata f1: this.grafo.vertexSet()) {
//			for(Fermata f2: this.grafo.vertexSet()) {
//				if(!f1.equals(f2) && dao.fermateCollegate(f1, f2)) {
//					this.grafo.addEdge(f1, f2) ;
//				}
//			}
//		}
		
		List<Connessione> connessioni = dao.getAllConnessioni(fermate) ;
		for(Connessione c: connessioni) {
			this.grafo.addEdge(c.getStazP(), c.getStazA()) ;
		}
		
		System.out.format("Grafo creato con %d vertici e %d archi\n",
				this.grafo.vertexSet().size(), this.grafo.edgeSet().size()) ;
//		System.out.println(this.grafo) ;
		
		//Fermata f = new Fermata(0, null, null);
		/*
		Set<DefaultEdge> archi = this.grafo.edgesOf(f);
		for(DefaultEdge e : archi) {
			Fermata f1 = Graphs.getOppositeVertex(grafo, e, f);
		}*/
		
		//List<Fermata> fermateAdiacenti = Graphs.successorListOf(this.grafo, f);
	}
	
	public List<Fermata> fermateRaggiungibili(Fermata partenza){
		BreadthFirstIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<>(this.grafo, partenza);
		//DepthFirstIterator<Fermata, DefaultEdge> dfv = new DepthFirstIterator<>(this.grafo, partenza);
		
		this.predecessore = new HashMap<>();
		this.predecessore.put(partenza, null);
		
		//Il metodo addTraversalListener richiede come prarametro un oggetto che implementi l'interfaccia
		//TraversalListener e lo creo inline tra parentesi quadre (facendomi aiutare dai wizard di java)
		bfv.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {	
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
				//Adesso poichà il grafo non è orientato devo scoprire per l'arco e
				//Quali sono il predecessore ed il successore tra i vertici ai
				//suoi estermi e li aggiungo alla mappa predecessore.
				DefaultEdge arco = e.getEdge();
				Fermata a = grafo.getEdgeSource(arco);
				Fermata b = grafo.getEdgeTarget(arco);
				if(predecessore.containsKey(b) && !predecessore.containsKey(a)) {
					//se conoscevo già b allora è predessore di a (ma lo aggiungo solo
					//se non conoscevo a)
					predecessore.put(a, b);
				}//Se però non conoscevo b, allora a è suo predecessore e b è nuovo
				//ma lo aggiungo solo se conoscevo già a
				else if(predecessore.containsKey(a) && !predecessore.containsKey(b))
					predecessore.put(b, a);
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> e) {
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> e) {
			}
			
		});
		
		List<Fermata> result = new ArrayList<>();
		while(bfv.hasNext()) {
			Fermata f = bfv.next();
			result.add(f);
		}
		return result;
	}
	
	public Fermata trovaFermata(String nome) {
		for(Fermata f : this.grafo.vertexSet()) {
			if(f.getNome().equals(nome))
				return f;
		}
		return null;
	}
	
	public List<Fermata> trovaCammino(Fermata partenza, Fermata arrivo){
		fermateRaggiungibili(partenza);
		
		//Riempio la lista del cammino a partire dalla fermata di arrivo
		//usando la mappa predecessore
		List<Fermata> result = new ArrayList<>();
		result.add(arrivo);
		Fermata f = arrivo;
		while(this.predecessore.get(f)!=null) {
			f = this.predecessore.get(f);
			result.add(f);
			
		}
		return result;
	}
	
}
