package ma.project.elharrassafaa.ui.Home;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ma.project.elharrassafaa.R;

public class HomeFragment extends Fragment {
    private EditText nomEmploye;
    private EditText prenomEmploye;
    private EditText dateEmploye;


    private Button bnAddEmploye;
    private String insertEmployeUrl = "http://10.0.2.2:8080/api/employes";
    private String listEmployeUrl = "http://10.0.2.2:8080/api/employes";
    private String listServiceUrl = "http://10.0.2.2:8080/api/cities";

    LinearLayout employeListLayout;
    Spinner spinnerService;

    private List<String> servicesList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        nomEmploye = view.findViewById(R.id.nom);
        prenomEmploye = view.findViewById(R.id.prenom);
        dateEmploye= view.findViewById(R.id.date);
        bnAddEmploye = view.findViewById(R.id.bnAdd);
        employeListLayout = view.findViewById(R.id.employeListLayout);
        spinnerService = view.findViewById(R.id.serviceSpinner);

        bnAddEmploye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEmployeToDatabase();
            }
        });

        spinnerService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Faites quelque chose avec la valeur sélectionnée si nécessaire
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Gérer l'événement si rien n'est sélectionné si nécessaire
            }
        });

        fetchDataAndPopulateEmployeList();
        fetchServices();

        return view;
    }


    private void addEmployeToDatabase() {
        String selectedService = spinnerService.getSelectedItem().toString();
        // Ajoutez les autres informations nécessaires pour la demande d'ajout
        int serviceId = getServiceIdByName(selectedService);
        String nomText = nomEmploye.getText().toString();
        String prenomText = prenomEmploye.getText().toString();
        String dateText = dateEmploye.getText().toString();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", 0);
            jsonBody.put("nom", nomText);
            jsonBody.put("prenom", prenomText);
            jsonBody.put("dateNaissance", dateText);

            // Ajoutez les autres informations nécessaires, y compris la clé étrangère du bâtiment
            JSONObject serviceObject = new JSONObject();
            serviceObject.put("id", serviceId);
            jsonBody.put("service", serviceObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, insertEmployeUrl, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleAddEmployeResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleErrorAddingEmploye(error);
            }
        });

        requestQueue.add(request);
    }
    private int getServiceIdByName(String serviceName) {
        for (int i = 0; i < servicesList.size(); i++) {
            if (servicesList.get(i).equals(serviceName)) {
                return i + 1;
            }
        }
        return -1;
    }
    private void handleAddEmployeResponse(JSONObject response) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage("Ajout d'employe avec succès")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                fetchDataAndPopulateEmployeList();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void handleErrorAddingEmploye(VolleyError error) {
        Log.e("Fragment", "Error adding employe: " + error.getMessage());
    }

    private void fetchDataAndPopulateEmployeList() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, listEmployeUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        employeListLayout.removeAllViews();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject employeObject = response.getJSONObject(i);
                                String employeId = employeObject.getString("id");
                                String employeNom= employeObject.getString("nom");
                                String employePrenom= employeObject.getString("prenom");
                                String employeDate= employeObject.getString("dateNaissance");

                                String service = employeObject.getJSONObject("service").getString("nom");

                                View listItemView = LayoutInflater.from(requireContext()).inflate(R.layout.list_home, null);

                                TextView nomTextView = listItemView.findViewById(R.id.TextView1);
                                TextView prenomTextView = listItemView.findViewById(R.id.TextView2);
                                TextView dateTextView = listItemView.findViewById(R.id.TextView3);


                                TextView serviceTextView = listItemView.findViewById(R.id.TextView4);


                                serviceTextView.setText(service);
                                nomTextView.setText(employeNom);
                                prenomTextView.setText(employePrenom);
                                dateTextView.setText(employeDate);




                                listItemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                });

                                employeListLayout.addView(listItemView);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Fragment", "Error fetching employe data: " + error.getMessage());
            }
        });

        requestQueue.add(request);
    }





    private void fetchServices() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, listServiceUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        servicesList.clear();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject serviceObject = response.getJSONObject(i);
                                String service = serviceObject.getString("nom");
                                servicesList.add(service);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, servicesList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerService.setAdapter(adapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(requireContext(), "Erreur lors de la récupération des services", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(request);
    }
}