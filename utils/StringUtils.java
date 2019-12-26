package utils;

import java.util.StringTokenizer;

public class StringUtils{
		
		public static int [] stringToIntArray(String string) {
			//Gera uma exception se não estiver na notação de conjunto
			if(!string.startsWith("{") || !string.endsWith("}")) {
				throw new RuntimeException("Erro: We can't convert the string to int array");
			}
			
			//Obtém a parte contendo os números
			string = string.substring(1, string.length() - 1);
			StringTokenizer st = new StringTokenizer(string, ",");
			
			//Cria o array de retorno no tamanho correto
			int [] return_array = new int[st.countTokens()];
			
			int i = 0;
			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				return_array[i++] = Integer.parseInt(token);
			}

			return return_array;
		}
		
		public static String intArrayToString(int[] array) {
			StringBuilder ret = new StringBuilder();
			ret.append("{");
			for(int i = 0; i < array.length; i++) {
				ret.append(array[i]);
				
				if(i < array.length-1)
					ret.append(",");
				
			}
			ret.append("}");
			
			return ret.toString();
		}
}

