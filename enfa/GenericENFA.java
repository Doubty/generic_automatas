package enfa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import utils.StringUtils;

public class GenericENFA {
	
	private int initial_state;
	private int [] admission_states;
	private int [][][] transitions;
	private int [][] empty_transitions;
	private String input_mapping;
	private boolean show_ic;
	
	public static void main(String args[]) {
		String caminho = System.getProperty("user.dir") + "/src/enfa/configuracoes.txt";
		try {
			
			GenericENFA ENFA = new GenericENFA(new BufferedReader(new FileReader(new File(caminho))), true);
			//System.out.println(ENFA);
			
			//Sequência de entradas para testar
			String [] inputs = new String[] {"134.500", "","123","-30"};
			
			//Verifica as entradas
			for(String s : inputs) {
				System.out.println("------------------------------");
				System.out.println("Input: " + s);
				boolean accept = ENFA.execute(s);
				
				if(accept) {
					System.out.println("Accept");
					System.out.println("------------------------------\n");
				}else {
					System.out.println("Reject");
					System.out.println("------------------------------\n");
				}
			}
			
			
		} catch (FileNotFoundException e) {
			System.out.println("Erro reading the file\n");
			e.printStackTrace();
		}
	}
	
	public GenericENFA(BufferedReader config, boolean show_ic) {
		this(config);
		this.show_ic = show_ic;
	}
	
	public GenericENFA(BufferedReader config) {
		this.show_ic = false;
		try {
			//Lê o estado inicial
			String line = config.readLine();
			int i = line.indexOf(':') + 1;
			this.initial_state = Integer.parseInt(line.substring(i).trim());
			
			//Lê os estados de aceitação
			line = config.readLine();
			i = line.indexOf(':') + 1;
			this.admission_states = StringUtils.stringToIntArray(line.substring(i).trim());
			
			//Lê o número de estados
			line = config.readLine();
			i = line.indexOf(':') + 1;
			int number_states = Integer.parseInt(line.substring(i).trim());
			
			//Lê a quarta linha que demarca o início da tabela de transições
			config.readLine();
			
			//Lê o alfabeto;
			line = config.readLine();
			StringTokenizer st = new StringTokenizer(line);
			int symbols_number = st.countTokens()-1;
			int symbols_number_extended = 0;		
			int [] expand = new int[symbols_number];
			i = 0;
			
			StringBuilder input_mapping = new StringBuilder();
			
			//Verifica se é um símbolo único ou uma sequência Ex: 1-3
			while(st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				//Se não for símbolo vazio, varre e faz o mapeamento
				if(!token.equals("{}")) {
					//Se for um símbolo único
					if(token.length() == 1) {
						input_mapping.append(token);
						expand[i++] = 1;
						symbols_number_extended++;
					}else {
					//Se for uma sequência de símbolos
						if(token.length() != 3) {
							throw new RuntimeException("Erro reading symbol, invalid format");
						}
						char start = token.charAt(0);
						char end = token.charAt(2);
						StringBuilder expanded = new StringBuilder();
						for(int c = start; c<=end;c++) {
							expanded.append((char) c);
						}
						input_mapping.append(expanded.toString());
						expand[i++] = expanded.length();
						symbols_number_extended += expanded.length();
					}
				}
			}
			//Inicializa a tabela de transições
			transitions = new int[number_states][symbols_number_extended][];
			empty_transitions = new int[number_states][];
			
			//Preenche as tabelas de transições
			while(!(line = config.readLine()).equals("END")) {
				//Lê a linha e obtém o número do estado
				st = new StringTokenizer(line);
				int state = Integer.parseInt(st.nextToken().trim());
				int alpha_index = 0;
				i = 0;
				
				//Percorre as transições
				while(st.hasMoreTokens()) {
					//Obtém as transições
					String str_transition = st.nextToken().trim();
					//Passa pro formato de Array de Int
					int [] destiny_transition = StringUtils.stringToIntArray(str_transition);
					//Quando há transições em sequência Ex: 1-3
					if(st.hasMoreElements()) {
						for(int j = 0; j < expand[alpha_index]; j++) {
							transitions[state][i++] = destiny_transition;
						}
					}else {
						//Quando é a transição vazia
						empty_transitions[state] = destiny_transition;
					}
					alpha_index++;
				}
			}
			
			//Atribui o mapa de entrada para a variável de controle 
			this.input_mapping  = input_mapping.toString();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean execute(String input) {
				
		int [] actual_states = eclose(new int[]{initial_state});
		
		int position = 0;
				
		while(position < input.length()) {
			//Mostra a configuração instantânea se estiver setado true
			if(this.show_ic) {
				showIC(input, actual_states, position);
			}
			
			//Pega o símbolo da cadeia
			String element = input.substring(position, position+1);
			
			int [] final_states = new int[] {};	
			
			for(int state : actual_states) {
				
				//Encontra a posição na tabela dos símbolos
				int position_map = this.input_mapping.indexOf(element);
				if(position_map == -1) {
					return false;
				}
				
				int [] destiny_transiction = this.transitions[state][position_map];
				final_states = union(final_states, destiny_transiction);
				final_states = eclose(final_states);
			}
			
			actual_states = final_states;
			
			if(actual_states.length == 0) {
				break;
			}
			
			position++;
		}
		
		if(this.show_ic) {
			showIC(input, actual_states, position);
		}
		
		if(isAccept(actual_states)) {
			return true;
		}else {
			return false;
		}
		
		
	}
	
	//Mostra as configurações instantaneas
	public void showIC(String input, int [] states, int position) {
		System.out.print(input.substring(0, position) + "{");
		for(int i = 0; i < states.length; i++) {
			System.out.print("q" + states[i]);
			if(i<states.length - 1) {
				System.out.print(",");
			}
		}
		System.out.println("}" + input.substring(position));
	}
	
	//Verifica se o autômato aceita a cadeia
	//testando se o estado final é de aceitação
	public boolean isAccept(int [] final_states) {
		
		if(final_states == null) return false;
		
		//Verifica se o estado final é de aceitação
		for(int admission_state : this.admission_states) {
			for(int final_state : final_states) {
				if(admission_state == final_state) { 
					return true;
				}				
			}
		}
		
		return false;
	}
	
	//Faz a união dos estados
	private static int[] union(int[] final_states, int[] destiny_transiction) {
		Set<Integer> union = new TreeSet<>();
		for(int state : final_states) union.add(state);
		for(int state : destiny_transiction) union.add(state);
		
		//Converter o Set para um Array primitivo usando a API Stream
		int [] return_states = union.stream().mapToInt(Integer::intValue).toArray();
		return return_states;
	}
	
	private int[] eclose(int[] states) {
		int [] eclose = states;
		
		for(int state : states) {
			int [] eclose_aux = this.empty_transitions[state];
			int [] eclose_aux2 = eclose(eclose_aux);
			
			eclose = union(eclose, eclose_aux);
			eclose = union(eclose, eclose_aux2);
		}
		
		return eclose;
	}
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		
		//Imprimir configurações da máquina
		ret.append("Init state: " + this.initial_state + "\n");
		ret.append("Admission states: " + StringUtils.intArrayToString(this.admission_states) + "\n");
		ret.append("Input mapping: " + this.input_mapping + "\n");
		ret.append("Transitions: \n");
		int i = 0;
				
		for(int[][] states : this.transitions) {
			ret.append((i++) + ": ");
			
			for(int [] t : states) {
				ret.append(StringUtils.intArrayToString(t));
			}
			ret.append("\n");		
		}
				
		ret.append("Empty transitions: \n");
		i = 0;
		for(int [] t : this.empty_transitions) {
			ret.append((i++) + ": ");
			ret.append(StringUtils.intArrayToString(t));
			ret.append("\n");
			
		}
		
		return ret.toString();
	}
}
