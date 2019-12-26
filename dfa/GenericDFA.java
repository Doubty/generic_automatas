package dfa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.StringTokenizer;
import utils.StringUtils;

public class GenericDFA {
	
	private int initial_state;
	private int [] admission_states;
	private int [][] transitions;
	private String input_mapping;
	private boolean show_ic;
	
	public static void main(String args[]) {
		String caminho = System.getProperty("user.dir") + "/src/dfa/configuracoes.txt";
		try {
			
			GenericDFA DFA = new GenericDFA(new BufferedReader(new FileReader(new File(caminho))), true);
			System.out.println(DFA);		
			
			//Vetor de entradas
			String [] inputs = new String[] {"010", "1","1010"};
			
			//Percorre as cadeias testando
			for(String s : inputs) {
				System.out.println("------------------------");
				System.out.println("Input: " + s);
				
				boolean accept = DFA.execute(s);
				
				if(accept){
					System.out.println("Accept");
				}else {
					System.out.println("Reject");
				}

				System.out.println("------------------------\n");
			}
			
			
		} catch (FileNotFoundException e) {
			System.out.println("Erro reading the file\n");
			e.printStackTrace();
		}
	}
	
	public GenericDFA(BufferedReader config, boolean show_ic) {
		this(config);
		this.show_ic = show_ic;
	}
	
	public GenericDFA(BufferedReader config) {
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
			int symbols_number = st.countTokens();
			int symbols_number_extended = 0;		
			int [] expand = new int[symbols_number];
			i = 0;
			
			StringBuilder input_mapping = new StringBuilder();
			
			//Verifica se é um símbolo único ou uma sequência Ex: 1-3
			while(st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				
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
			
			//Inicializa a tabela de transições
			transitions = new int[number_states][symbols_number_extended];
			
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
					int destiny_transition = Integer.parseInt(str_transition);
					//Quando há transições em sequência Ex: 1-3
					for(int j = 0; j < expand[alpha_index]; j++) {
						transitions[state][i++] = destiny_transition;
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
		//Estado inicial
		int actual_state = this.initial_state;
				
		//Variável de controle para mostrar a configuração instantanea
		int position = 0; 
				
		//Percorre a cadeia
		while(position < input.length()) {
			//Mostra a configuração instantânea
			if(this.show_ic) {
				showIC(input, actual_state, position);
			}
			String symbol = input.substring(position, position+1);
			
			int position_map = this.input_mapping.indexOf(symbol);
			//Passado um caracter não mapeado
			if(position_map == -1) {
				return false;
			}
			
			actual_state = transitions[actual_state][position_map];
				
			position++;
		}
		
		if(this.show_ic) {
			showIC(input, actual_state, position);
		}
		
		if(isAccept(actual_state)) {
			return true;
		}else {
			return false;
		}
		
	}
	
	public void showIC(String input, int estate, int position) {
		System.out.print(input.substring(0, position));
		System.out.print("[q" + estate + "]");
		System.out.println(input.substring(position));
	}
	
	public boolean isAccept(int final_state) {
		boolean accept = false;
		
		//Verifica se o estado final é de aceitação
		for(int state : this.admission_states) {
			if(final_state == state) accept = true;
		}
		
		return accept;
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
				
		for(int[] states : this.transitions) {
			ret.append((i++) + ": ");
			
			for(int t : states) {
				ret.append(t + " ");
			}
			ret.append("\n");		
		}
		
		return ret.toString();
	}
}
