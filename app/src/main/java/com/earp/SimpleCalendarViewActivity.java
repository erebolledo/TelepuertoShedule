package com.earp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SimpleCalendarViewActivity extends Activity implements OnClickListener
	{
		private static final String tag = "SimpleCalendarViewActivity";
		private ImageView calendarToJournalButton;
		private Button selectedDayMonthYearButton;
		private Button currentMonth;
		private ImageView prevMonth;
		private ImageView nextMonth;
		private GridView calendarView;
		private GridCellAdapter adapter;
		private Calendar _calendar;
		public int month, year;
		private final DateFormat dateFormatter = new DateFormat();
		private static final String dateTemplate = "MMMM yyyy";
		public String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
		public int primerGrupo= 4;//ACA SE COLOCA EL PRIMER GRUPO DEL AÑO
		public Spinner spinner;
        public String[] grupos = {"A","B","C","D","E","F"};
		int[] diasDeMes = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		int grupoMio = 5;
		public ArrayList<String> feriados = new ArrayList<String>();
		public Feriados html = new Feriados();
		private static final int MNU_OPC1 = 1;
		private static final int MNU_OPC2 = 2;
		private static final int MNU_OPC3 = 3;
		

		/** Called when the activity is first created. */
		@Override
		public void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.simple_calendar_view);
				//this.setHasOptionsMenu(true);
				//setContentView(R.menu.main);
				
				_calendar = Calendar.getInstance(Locale.getDefault());
				month = _calendar.get(Calendar.MONTH) + 1;
				//Toast.makeText(this, month, Toast.LENGTH_LONG);
				year = _calendar.get(Calendar.YEAR);
				Log.d(tag, "Calendar Instance:= " + "Month: " + month + " " + "Year: " + year);

				//selectedDayMonthYearButton = (Button) this.findViewById(R.id.selectedDayMonthYear);
				//selectedDayMonthYearButton.setText("Selected: ");

				prevMonth = (ImageView) this.findViewById(R.id.prevMonth);
				prevMonth.setOnClickListener(this);

				currentMonth = (Button) this.findViewById(R.id.currentMonth);
				currentMonth.setText(dateFormatter.format(dateTemplate, _calendar.getTime()));

				nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
				nextMonth.setOnClickListener(this);				

				calendarView = (GridView) this.findViewById(R.id.calendar);

				iniTablaRotacion();

				//Seleccion de grupo
				spinner = (Spinner) findViewById(R.id.grupo_selec);
				ArrayAdapter<String> adapter1 = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_item, grupos);
				spinner.setAdapter(adapter1);
				spinner.setSelection(2);
				spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			        	grupoMio = spinner.getSelectedItemPosition();
						Log.d("grupoMio", String.valueOf(grupoMio));
						setGridCellAdapterToDate(month, year);
			         }

			         public void onNothingSelected(AdapterView<?> parent) {
			                // Do nothing, just another required interface callback
			         }
			    });
								
				// Initialised
				adapter = new GridCellAdapter(getApplicationContext(), R.id.calendar_day_gridcell, month, year);
				adapter.notifyDataSetChanged();
				calendarView.setAdapter(adapter);
				
				setGridCellAdapterToDate(month, year);
			}
		
		//Inicializar la tabla rotacion
		public void iniTablaRotacion(){
			TelepuertoBD telebd = new TelepuertoBD(this, "TelepuertoBD.db", null, 1);	 
			SQLiteDatabase db = telebd.getReadableDatabase();
			Cursor c = db.rawQuery("SELECT * FROM Rotacion WHERE ano=2013;", null);
			
			if (!c.moveToFirst()){
	        	Log.i("La tabla no existe","no existe");	 
		        db = telebd.getWritableDatabase();
		        db.execSQL("INSERT INTO Rotacion (ano, grupo) VALUES (2013, 4)");
		        int auxGrupo = 4;
		        for (int i=2013;i<2050;i++){		        	
		        	int nDias=365;
		        	if((i%4==0)&&((i%100!=0)||(i%400==0)))
		        		nDias=366;
		        	int aux=nDias+auxGrupo;
		        	auxGrupo = aux%6;
		        	int nextAno = i+1;		        	
		        	db.execSQL("INSERT INTO Rotacion (ano, grupo) VALUES (" + nextAno + ", " + auxGrupo +")");
		        }
		        db.execSQL("INSERT INTO Feriado (fecha, feriado) VALUES ('24 Diciembre 2013', 'Navidad')");
		        db.close();				
			}
		}
		
		//Crear la Base de Datos
		public void actualizarTablaRotacion(int ano, int grupo){
			//Abrimos la base de datos 'TelepuertoBD' en modo escritura
			TelepuertoBD telebd = new TelepuertoBD(this, "TelepuertoBD.db", null, 1);	 
	        SQLiteDatabase db = telebd.getWritableDatabase();
	        
	        db.execSQL("UPDATE Rotacion SET grupo="+grupo+" WHERE ano="+ano);
	        int auxGrupo = grupo;
	        for(int i=ano;i<2049;i++){
	        	Log.i("Entre a actualizar", "to actualizando");
	        	int nDias=365;
	        	if((i%4==0)&&((i%100!=0)||(i%400==0)))
	        		nDias=366;
	        	int aux=nDias+auxGrupo;
	        	auxGrupo = aux%6;
		        db.execSQL("UPDATE Rotacion SET grupo="+auxGrupo+" WHERE ano="+i+1);
	        }
	        db.close();
			setGridCellAdapterToDate(month, year);				
	        
	        //Si hemos abierto correctamente la base de datos
	        /*if(db != null)
	        {
	            //Insertamos 5 usuarios de ejemplo
	            for(int i=1; i<=5; i++)
	            {
	                //Generamos los datos
	                int codigo = i;
	                String nombre = "Usuario" + i;
	 
	                //Insertamos los datos en la tabla Usuarios
	                db.execSQL("INSERT INTO Usuarios (codigo, nombre) " +
	                           "VALUES (" + codigo + ", '" + nombre +"')");
	            }
	 
	            //Cerramos la base de datos
	            //db.close();
	        }*/
	        //Eliminar un registro
	        //db.execSQL("DELETE FROM Usuarios WHERE codigo=6 ");

	        //Actualizar un registro
	        //db.execSQL("UPDATE Usuarios SET nombre='usunuevo' WHERE codigo=6 ");
	        
	        //SQLiteDatabase db = telebd.getReadableDatabase();
	        //Cursor c = db.rawQuery(" SELECT codigo,nombre FROM Usuarios WHERE nombre='Usuario1' ", null);
	        //Nos aseguramos de que existe al menos un registro
	        /*if (c.moveToFirst()) {
	             //Recorremos el cursor hasta que no haya más registros
	             do {
	                  String codigo= c.getString(0);
	                  String nombre = c.getString(1);
					  Log.d("Base de datos", "Codigo "+codigo+" Nombre "+nombre);	                  
	             } while(c.moveToNext());
	        }*/	        
		}
		
		public void agregarFeriadoBD(String fecha, String feriado){
			TelepuertoBD telebd = new TelepuertoBD(this, "TelepuertoBD.db", null, 1);	 
			SQLiteDatabase db = telebd.getReadableDatabase();
			Cursor c = db.rawQuery("SELECT * FROM Feriado WHERE fecha='"+fecha+"'", null);
			Log.i("Hasta aqui voy bien", "voy bien");

			if (!c.moveToFirst()){
		        db = telebd.getWritableDatabase();
			    db.execSQL("INSERT INTO Feriado (fecha, feriado) VALUES ('"+fecha+"', '"+feriado+"')");
				db.close();
			}
			c.close();
		}
		
		public void actualizarFeriados(String html){
        	String tr[] = html.split("<tr>");
    		TelepuertoBD telebd = new TelepuertoBD(this, "TelepuertoBD.db", null, 1);
    		SQLiteDatabase db = telebd.getWritableDatabase();
        	
    		for(int i=1;i<tr.length;i++){
    			String td[]=tr[i].split("<td class='day redday'>");
    			String aux[] =td[1].split("</td><td class='month redday'>");
    			String aux2[] = aux[1].split("</td><td class='desc'>");
    			String aux3[] = aux2[1].split("<span class='subs'>");
    			String dia = aux[0];        	
    			String mes = aux2[0];
    			String fecha = dia+" "+mes+" "+_calendar.get(Calendar.YEAR);;
    			String feriado = aux3[0];
    			Cursor c = db.rawQuery("SELECT * FROM Feriado WHERE fecha='"+fecha+"'", null);
    			if (!c.moveToFirst())
    				db.execSQL("INSERT INTO Feriado (fecha, feriado) VALUES ('"+fecha+"', '"+feriado+"')");				
    			c.close();
            	Log.i("Cadena picada 223", dia+" "+mes+" "+year+" "+feriado+" longitud "+td.length);

    			aux =td[2].split("</td><td class='month redday'>");
    			aux2 = aux[1].split("</td><td class='desc'>");
    			aux3 = aux2[1].split("<span class='subs'>");
    			dia = aux[0];        	
    			mes = aux2[0];
    			fecha = dia+" "+mes+" "+_calendar.get(Calendar.YEAR);;
    			feriado = aux3[0];
    			c = db.rawQuery("SELECT * FROM Feriado WHERE fecha='"+fecha+"'", null);
    			if (!c.moveToFirst())
    				db.execSQL("INSERT INTO Feriado (fecha, feriado) VALUES ('"+fecha+"', '"+feriado+"')");				
    			c.close();
            	Log.i("Cadena picada 223", dia+" "+mes+" "+year+" "+feriado+" longitud "+td.length);            	
    		}
    		db.close();
		}
		
		public void agregarFeriado(){
         	//Genero un Layout para el dialog
         	LayoutInflater factory = LayoutInflater.from(this);
         	final View textEntryView = factory.inflate(R.layout.feriado, null);
         	final DatePicker fechaFeriado = (DatePicker) textEntryView.findViewById(R.id.datePicker1); 	            	
         	final EditText nombreFeriado = (EditText) textEntryView.findViewById(R.id.editText1); 	            	
         	
         	new AlertDialog.Builder(this)
         	.setTitle("Agregar Feriados")
         	.setView(textEntryView)
         	.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {						
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int dia = fechaFeriado.getDayOfMonth();
					int mes = fechaFeriado.getMonth();
					int ano = fechaFeriado.getYear();
					String fecha = dia+" "+meses[mes];					
					agregarFeriadoBD(fecha, nombreFeriado.getText().toString());
					Log.i("fecha del datepicker", fecha);
				}
			})
			.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {						
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			})
			.show();			
		}
		
		//Creando el menu de opciones
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	    	getMenuInflater().inflate(R.menu.main, menu);
	        return true;	        
	    }
	    
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	    	String acerca = "Guardias Telepuerto Version 0.9.1 Desarrollado por Erka Rebolledo";
	    	String grupos ="GRUPO A:\n Ender Perdomo\n Nelson Lopez\nGRUPO B:\n Jose Malpica\n Norberto Villamizar\n" +
	    				   "GRUPO C:\n Felix Esperandio\n Erka Rebolledo\nGRUPO D:\n Johan Zapata\n Frankgel Carreyo\n" +
	    				   "GRUPO E:\n Sthorys Prato\n Emigdio Faneitte\nGRUPO F:\n William Ortega\n Adriana Barrios";
	        switch (item.getItemId()) {
	        
	            case R.id.grupos:
	            	new AlertDialog.Builder(this)
	                .setTitle("Grupos")
	                .setMessage(grupos)
	                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) { 
	                        // do nothing
	                    }
	                 })
	                 .show();	            	
	                return true;
	                
	            case R.id.actualizarFeriado:
	            	String corte[] = html.getContenido(String.valueOf(_calendar.get(Calendar.YEAR))).split(" Festivos</th></tr>");
	            	corte = corte[1].split("</table>");
	            	actualizarFeriados(corte[0]);
	            	setGridCellAdapterToDate(month, year);
	                return true;
	                
	            case R.id.agregarFeriado:
	            	agregarFeriado();
	            	setGridCellAdapterToDate(month, year);
	            	return true;
	                
 	            case R.id.rotacion: 	            	
 	            	final String[] gru = {"A","B","C","D","E","F"};
 	            	final String[] an ={"2013","2014","2015","2016","2017","2018","2019","2020","2021","2022","2023","2024","2025",
 	            			            "2026","2027","2028","2029","2030","2031","2032","2033","2034","2035","2036","2037","2038",
 	            			            "2039","2040","2041","2042","2043","2044","2045","2046","2047","2048","2049"};
 	            	//Genero un Layout para el dialog
 	            	LayoutInflater factory = LayoutInflater.from(this);
 	            	final View textEntryView = factory.inflate(R.layout.rotacion, null);
 	            	//final EditText input1 = (EditText) textEntryView.findViewById(R.id.anoRotacion);
 	            	final Spinner spinner2 = (Spinner) textEntryView.findViewById(R.id.grupoRotacion); 	            	
 					ArrayAdapter<String> adap = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_item, gru);
 					spinner2.setAdapter(adap);
 	            	final Spinner spinner3 = (Spinner) textEntryView.findViewById(R.id.anoRotacion); 	            	
 					ArrayAdapter<String> adap1 = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_item, an);
 					spinner3.setAdapter(adap1);
 	            	
 	            	new AlertDialog.Builder(this)
 	            	.setTitle("Cambio de Rotacion")
 	            	.setView(textEntryView)
 	            	.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String grupo = String.valueOf(spinner2.getSelectedItemPosition());
							String ano = spinner3.getSelectedItem().toString();							
							Log.i("AlertDialog","Año "+ano+" Grupo "+grupo);
							actualizarTablaRotacion(Integer.parseInt(ano), Integer.parseInt(grupo));
						}
					})
					.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
					.show();
	                return true;
	                
	            case R.id.acerca:
	            	new AlertDialog.Builder(this)
	                .setTitle("Acerca de")
	                .setMessage(acerca)
	                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) { 
	                        // do nothing
	                    }
	                 })
	                 .show();
	                return true;
	                
	            default:
	                return super.onOptionsItemSelected(item);
	        }
	    }
		
		/**
		 * 
		 * @param month
		 * @param year
		 */
		private void setGridCellAdapterToDate(int month, int year)
			{
				TelepuertoBD telebd = new TelepuertoBD(this, "TelepuertoBD.db", null, 1);	 
				SQLiteDatabase db = telebd.getReadableDatabase();
				Cursor c = db.rawQuery("SELECT * FROM Rotacion WHERE ano="+year, null);
				c.moveToFirst();
				primerGrupo = Integer.parseInt(c.getString(1));
				c = db.rawQuery("SELECT * FROM Feriado", null);

				while (c.moveToNext())	
					feriados.add(c.getString(0));
				c.close();
				db.close();
				Log.d("Hasta aqui voy bien 352",feriados.toString());				

				adapter = new GridCellAdapter(getApplicationContext(), R.id.calendar_day_gridcell, month, year);
				_calendar.set(year, month - 1, _calendar.get(Calendar.DAY_OF_MONTH));
				//Toast.makeText(this, meses[_calendar.getTime().getMonth()], Toast.LENGTH_LONG).show();
				currentMonth.setText(meses[month-1]+" "+String.valueOf(year));
				adapter.notifyDataSetChanged();
				calendarView.setAdapter(adapter);				
			}

		@Override
		public void onClick(View v)
			{			
				if (v == prevMonth)
					{
						if (month == 1)
							{
							Log.d(tag+" Linea 416", "el mes es "+String.valueOf(month));
								month = 12;
								year--;
							}
						else
							{
								month--;
							}
						Log.d(tag, "Setting Prev Month in GridCellAdapter: " + "Month: " + month + " Year: " + year);
						setGridCellAdapterToDate(month, year);
					}
				if (v == nextMonth)
					{
						if (month == 12)
							{
								month = 1;
								year++;
							}
						else
							{
								month++;
							}
						Log.d(tag, "Setting Next Month in GridCellAdapter: " + "Month: " + month + " Year: " + year);
						setGridCellAdapterToDate(month, year);
					}
				//mesGlobal = month;
				//anoGlobal = year;
			}

		@Override
		public void onDestroy()
			{
				Log.d(tag, "Destroying View ...");
				super.onDestroy();
			}

		// ///////////////////////////////////////////////////////////////////////////////////////
		// Inner Class
		public class GridCellAdapter extends BaseAdapter implements OnClickListener
			{
				private static final String tag = "GridCellAdapter";
				private final Context _context;

				private final List<String> list;
				private static final int DAY_OFFSET = 1;
				private final String[] weekdays = new String[]{"Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab"};
				private final String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
				private final int[] daysOfMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
				private final int month, year;
				private int daysInMonth, prevMonthDays;
				private int currentDayOfMonth;
				private int currentWeekDay;
				private Button gridcell;
				private TextView num_events_per_day;
				private final HashMap eventsPerMonthMap;
				private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy");

				// Days in Current Month
				public GridCellAdapter(Context context, int textViewResourceId, int month, int year)
					{
						super();
						this._context = context;
						this.list = new ArrayList<String>();
						this.month = month;
						this.year = year;

						Log.d(tag, "==> Passed in Date FOR Month: " + month + " " + "Year: " + year);
						Calendar calendar = Calendar.getInstance();
						setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
						setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
						Log.d(tag, "New Calendar:= " + calendar.getTime().toString());
						Log.d(tag, "CurrentDayOfWeek :" + getCurrentWeekDay());
						Log.d(tag, "CurrentDayOfMonth :" + getCurrentDayOfMonth());

						// Print Month
						printMonth(month, year);

						// Find Number of Events
						eventsPerMonthMap = findNumberOfEventsPerMonth(year, month);
					}
				private String getMonthAsString(int i)
					{
						return meses[i];
					}

				private String getWeekDayAsString(int i)
					{
						return weekdays[i];
					}

				private int getNumberOfDaysOfMonth(int i)
					{
						return daysOfMonth[i];
					}

				public String getItem(int position)
					{
						return list.get(position);
					}

				@Override
				public int getCount()
					{
						return list.size();
					}
				
				

				//Obtengo la sumatoria de los dias anteriores al mes en curso
				public int diasAnt(int mesActual){
					//En caso de año bisiesto
					int aux1 = _calendar.get(Calendar.YEAR);
					if ((aux1 % 4 == 0) && ((aux1 % 100 != 0) || (aux1 % 400 == 0)))					
						diasDeMes[1]=29;
					
					int aux=0;					
					for(int i =0;i<mesActual;i++){
						aux+=diasDeMes[i];
					}
					//Toast.makeText(getApplicationContext(), String.valueOf(_calendar.get(Calendar.YEAR)), Toast.LENGTH_LONG).show();
					diasDeMes[1]=28;
					return aux;
				}
				
				//Obtengo el grupo q comienza la guardia del mes en curso
				public int grupoActual(int primerGrupo, int diasA){
					int aux=primerGrupo;
					for (int i=0;i<diasA;i++){						
						aux++;
						if(aux==6)aux=0;
					}
					Log.d("Dias anteriores", String.valueOf(String.valueOf(diasA)));					
					return aux;
				}
				
				//Obtengo el siguiente grupo en enteros del 0 al 5
				public int rotacion(int grupo, boolean inv){
					if (inv)
						grupo--;
					else
						grupo++;
					if (grupo==6){grupo =0;}
					if (grupo==-1){grupo=5;}
					Log.d("rotacion", String.valueOf(grupo));
					return grupo;
				}
				/**
				 * Prints Month
				 * 
				 * @param mm
				 * @param yy
				 */
				private void printMonth(int mm, int yy)
					{
						Log.d(tag, "==> printMonth: mm: " + mm + " " + "yy: " + yy);
						// The number of days to leave blank at
						// the start of this month.
						int trailingSpaces = 0;
						int leadSpaces = 0;
						int daysInPrevMonth = 0;
						int prevMonth = 0;
						int prevYear = 0;
						int nextMonth = 0;
						int nextYear = 0;

						int currentMonth = mm - 1;
						String currentMonthName = getMonthAsString(currentMonth);
						daysInMonth = getNumberOfDaysOfMonth(currentMonth);

						Log.d(tag, "Current Month: " + " " + currentMonthName + " having " + daysInMonth + " days.");

						// Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
						GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1);
						Log.d(tag, "Gregorian Calendar:= " + cal.getTime().toString());

						if (currentMonth == 11)
							{
								prevMonth = currentMonth - 1;
								daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
								nextMonth = 0;
								prevYear = yy;
								nextYear = yy + 1;
								Log.d(tag, "*->PrevYear: " + prevYear + " PrevMonth:" + prevMonth + " NextMonth: " + nextMonth + " NextYear: " + nextYear);
							}
						else if (currentMonth == 0)
							{
								prevMonth = 11;
								prevYear = yy - 1;
								nextYear = yy;
								daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
								nextMonth = 1;
								Log.d(tag, "**--> PrevYear: " + prevYear + " PrevMonth:" + prevMonth + " NextMonth: " + nextMonth + " NextYear: " + nextYear);
							}
						else
							{
								prevMonth = currentMonth - 1;
								nextMonth = currentMonth + 1;
								nextYear = yy;
								prevYear = yy;
								daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
								Log.d(tag, "***---> PrevYear: " + prevYear + " PrevMonth:" + prevMonth + " NextMonth: " + nextMonth + " NextYear: " + nextYear);
							}

						// Compute how much to leave before before the first day of the
						// month.
						// getDay() returns 0 for Sunday.
						int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
						trailingSpaces = currentWeekDay;

						Log.d(tag, "Week Day:" + currentWeekDay + " is " + getWeekDayAsString(currentWeekDay));
						Log.d(tag, "No. Trailing space to Add: " + trailingSpaces);
						Log.d(tag, "No. of Days in Previous Month: " + daysInPrevMonth);

						if (cal.isLeapYear(cal.get(Calendar.YEAR)) && mm == 2)
							{
								++daysInMonth;
							}

						// Trailing Month days
						for (int i = 0; i < trailingSpaces; i++)
							{
								Log.d(tag, "PREV MONTH:= " + prevMonth + " => " + getMonthAsString(prevMonth) + " " + String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET) + i));
								list.add(String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET) + i) + "-GREY" + "-" + getMonthAsString(prevMonth) + "-" + prevYear);
							}
						
						//Log.d("mensaje", String.valueOf(grupoActual(primerGrupo, diasAnt(currentMonth))));
						Log.d("mensaje2", spinner.getSelectedItem().toString());
						int aux = grupoActual(primerGrupo, diasAnt(currentMonth));
						Log.d("grupo actual ","grupo actual "+String.valueOf(aux));
						int aux1 = rotacion(aux, true);
						int aux2 = rotacion(aux1, true);
						// Current Month Days
						for (int i = 1; i <= daysInMonth; i++)
							{
								aux1 = rotacion(aux, true);
								aux2 = rotacion(aux1, true);

								if (aux==grupoMio){
									list.add(String.valueOf(i) + "-BLUE" + "-" + getMonthAsString(currentMonth) + "-" + yy+"-toca");
									if (i+1<=daysInMonth)
										list.add(String.valueOf(i+1) + "-BLUE" + "-" + getMonthAsString(currentMonth) + "-" + yy+"-toca");
									if (i+2<=daysInMonth)
										list.add(String.valueOf(i+2) + "-BLUE" + "-" + getMonthAsString(currentMonth) + "-" + yy+"-toca");
									i+=2;
									aux=rotacion(aux, false);
									aux=rotacion(aux, false);
									aux=rotacion(aux, false);									
								}else{
									String col;
									col = "-WHITE";
									if (grupoMio==aux1) col = "-BLUE";
									if (grupoMio==aux2) col = "-BLUE";
									list.add(String.valueOf(i) + col + "-" + getMonthAsString(currentMonth) + "-" + yy+"-notoca");
									aux=rotacion(aux, false);
								}
								/*if (grupoMio==aux1){
									list.add(String.valueOf(i-1) + "-BLUE" + "-" + getMonthAsString(currentMonth) + "-" + yy+"-toca");
								}else if(grupoMio==aux2){
									list.add(String.valueOf(i-2) + "-BLUE" + "-" + getMonthAsString(currentMonth) + "-" + yy+"-toca");
								}*/
							
								/*Log.d("La vaina es aqui "+currentMonthName, String.valueOf(i) + " " + getMonthAsString(currentMonth) + " " + yy);
								if (i == getCurrentDayOfMonth())
									{
										list.add(String.valueOf(i) + "-BLUE" + "-" + getMonthAsString(currentMonth) + "-" + yy);
									}
								else
									{
										list.add(String.valueOf(i) + "-WHITE" + "-" + getMonthAsString(currentMonth) + "-" + yy);
									}*/								
							}
						/*Calendar c1 = Calendar.getInstance();
						int mes1=c1.get(Calendar.MONTH);
						int ano1=c1.get(Calendar.YEAR);
						for (int i=0;i<daysInMonth;i++){
							Log.d("fecha actual", "mes calendario "+String.valueOf(mm)+" mes actual "+String.valueOf(mes1)+String.valueOf(ano1)+String.valueOf(yy));
							if ((i == getCurrentDayOfMonth())&&(mes1==currentMonth)&&(ano1==yy))							
								list.add(String.valueOf(i) + "-RED" + "-" + getMonthAsString(currentMonth) + "-" + yy);							
						}*/

						// Leading Month days
						for (int i = 0; i < list.size() % 7; i++)
							{
								Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
								list.add(String.valueOf(i + 1) + "-GREY" + "-" + getMonthAsString(nextMonth) + "-" + nextYear+"-notoca");
							}
					}

				/**
				 * NOTE: YOU NEED TO IMPLEMENT THIS PART Given the YEAR, MONTH, retrieve
				 * ALL entries from a SQLite database for that month. Iterate over the
				 * List of All entries, and get the dateCreated, which is converted into
				 * day.
				 * 
				 * @param year
				 * @param month
				 * @return
				 */
				private HashMap findNumberOfEventsPerMonth(int year, int month)
					{
						HashMap map = new HashMap<String, Integer>();
						// DateFormat dateFormatter2 = new DateFormat();
						//						
						// String day = dateFormatter2.format("dd", dateCreated).toString();
						//
						// if (map.containsKey(day))
						// {
						// Integer val = (Integer) map.get(day) + 1;
						// map.put(day, val);
						// }
						// else
						// {
						// map.put(day, 1);
						// }
						return map;
					}

				@Override
				public long getItemId(int position)
					{
						return position;
					}

				@Override
				public View getView(int position, View convertView, ViewGroup parent)
					{
						View row = convertView;
						LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						Log.i(tag, "Current Day: " + getCurrentDayOfMonth());
						String[] day_color = list.get(position).split("-");

						if (row == null){
							if (day_color[1].equals("BLUE")){
								row = inflater.inflate(R.layout.guardias,parent,false);//calendar_day_gridcell, parent, false);
								gridcell = (Button) row.findViewById(R.id.guardias);//calendar_day_gridcell);
							}
							else{
								row = inflater.inflate(R.layout.calendar_day_gridcell, parent, false);
								gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);								
							}
						}

						// Get a reference to the Day gridcell

						//gridcell.setOnClickListener(this);

						// ACCOUNT FOR SPACING

						String theday = day_color[0];
						String themonth = day_color[2];
						String theyear = day_color[3];

						if ((!eventsPerMonthMap.isEmpty()) && (eventsPerMonthMap != null))
							{
								if (eventsPerMonthMap.containsKey(theday))
									{
										num_events_per_day = (TextView) row.findViewById(R.id.num_events_per_day);
										Integer numEvents = (Integer) eventsPerMonthMap.get(theday);
										num_events_per_day.setText(numEvents.toString());
									}
							}

						// Set the Day GridCell
						gridcell.setText(theday);
						gridcell.setTag(theday + "-" + themonth + "-" + theyear);
						String toca = day_color[0];

						if (day_color[1].equals("WHITE"))
							{
								gridcell.setTextColor(Color.WHITE);
							}
						if (day_color[1].equals("BLUE"))							
							{
								gridcell.setTextColor(Color.WHITE);
							}
						if (day_color[1].equals("RED"))							
						{
							//gridcell.setTextColor(Color.BLUE);
							//gridcell.setTextSize(gridcell.getTextSize()+10);
						}
						Calendar c = Calendar.getInstance();
						int dia = c.get(Calendar.DATE);
						String mes = meses[c.get(Calendar.MONTH)];
						int annio = c.get(Calendar.YEAR);
						String fecha1 =gridcell.getText()+" "+themonth+" "+theyear;
						String fecha2=dia+" "+mes+" "+annio;
						Log.d("el dia es",gridcell.getText()+" "+themonth+" "+theyear);
						Log.d("la fecha de hoy es",dia+" "+mes+" "+annio);
												
						String feriado = gridcell.getText()+" "+themonth+" "+theyear;
						Log.i("Resultado linea 765", feriado);
						if(feriados.contains(feriado))
							gridcell.setTextColor(Color.BLUE);

						if(fecha1.equals(fecha2))
							gridcell.setTextColor(Color.BLACK);

						if (day_color[1].equals("GREY"))
						{
							gridcell.setTextColor(Color.LTGRAY);
						}
						

						//if (day_color[4].equals("toca")) {gridcell.setTextColor(Color.RED);}

						return row;
					}
				@Override
				public void onClick(View view)
					{
						String date_month_year = (String) view.getTag();
						selectedDayMonthYearButton.setText("Selected: " + date_month_year);

						try
							{
								Date parsedDate = dateFormatter.parse(date_month_year);
								Log.d(tag, "Parsed Date: " + parsedDate.toString());

							}
						catch (ParseException e)
							{
								e.printStackTrace();
							}
					}

				public int getCurrentDayOfMonth()
					{
						return currentDayOfMonth;
					}

				private void setCurrentDayOfMonth(int currentDayOfMonth)
					{
						this.currentDayOfMonth = currentDayOfMonth;
					}
				public void setCurrentWeekDay(int currentWeekDay)
					{
						this.currentWeekDay = currentWeekDay;
					}
				public int getCurrentWeekDay()
					{
						return currentWeekDay;
					}
			}
	}
