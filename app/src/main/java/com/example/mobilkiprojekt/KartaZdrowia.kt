package com.example.mobilkiprojekt

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobilkiprojekt.data.AppDatabase
import com.example.mobilkiprojekt.data.CatDao
import com.example.mobilkiprojekt.data.CatEntity
import com.example.mobilkiprojekt.data.MedicalEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.UUID

// Prosty ViewModel do zarządzania stanem i interakcją z DAO
class KartaZdrowiaViewModel(private val catDao: CatDao) : androidx.lifecycle.ViewModel() {

    // Przepływ (Flow) przechowujący listę kotów z bazy danych
    val catProfiles: StateFlow<List<CatEntity>> = catDao.getAllCats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Rozpocznij subskrypcję, gdy ekran jest widoczny
            initialValue = emptyList() // Wartość początkowa
        )

    // Funkcja do dodawania lub aktualizowania kota w bazie danych
    fun upsertCat(cat: CatEntity) {
        viewModelScope.launch(Dispatchers.IO) { // Operacje bazodanowe w tle
            catDao.insert(cat) // Używamy insert z OnConflictStrategy.REPLACE
        }
    }

    // Funkcja do usuwania kota z bazy danych
    fun deleteCat(cat: CatEntity) {
        viewModelScope.launch(Dispatchers.IO) { // Operacje bazodanowe w tle
            catDao.delete(cat)
        }
    }

    // Funkcja do dodawania wpisu medycznego do konkretnego kota
    fun addMedicalEventToCat(catId: Long, event: MedicalEventEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val cat = catDao.getCatById(catId) // Pobierz aktualny stan kota
            cat?.let {
                // Dodaj nowe zdarzenie do istniejącej listy (tworząc nową listę)
                val updatedHistory = it.medicalHistory + event
                // Utwórz zaktualizowaną encję kota
                val updatedCat = it.copy(medicalHistory = updatedHistory)
                // Zapisz zaktualizowanego kota w bazie
                catDao.update(updatedCat)
                withContext(Dispatchers.Main) {
                    catDao.getAllCats()
                }
            }
        }
    }

    // Funkcja do usuwania wpisu medycznego
    fun removeMedicalEventFromCat(catId: Long, eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cat = catDao.getCatById(catId)
            cat?.let {
                val updatedHistory = it.medicalHistory.filterNot { event -> event.id == eventId }
                val updatedCat = it.copy(medicalHistory = updatedHistory)
                catDao.update(updatedCat)
                withContext(Dispatchers.Main) {
                    catDao.getAllCats() // Odświeżenie Flow
                }
            }
        }
    }
}

// Do tworzenia instancji KartaZdrowiaViewModel z DAO
class KartaZdrowiaViewModelFactory(private val catDao: CatDao) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KartaZdrowiaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KartaZdrowiaViewModel(catDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@Composable
fun KartaZdrowiaScreen(navController: NavController) {
    val context = LocalContext.current
    val catDao = remember { AppDatabase.getDatabase(context).catDao() }
    val viewModel: KartaZdrowiaViewModel = viewModel(factory = KartaZdrowiaViewModelFactory(catDao))
    val catProfiles by viewModel.catProfiles.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showAddMedicalEventDialog by remember { mutableStateOf(false) }
    var selectedCat by remember { mutableStateOf<CatEntity?>(null) }
    var editingCat by remember { mutableStateOf<CatEntity?>(null) } // Kot do edycji/dodania

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.ciemny))
    ) {
        // Przycisk powrotu
        IconButton(
            onClick = {
                navController.navigateUp()
            },
            modifier = Modifier
                .padding(top = 46.dp, start = 16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Powrót",
                tint = colorResource(id = R.color.kremowy)
            )
        }

        // Nagłówek
        Text(
            text = "Karta Zdrowia",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = colorResource(id = R.color.kremowy),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
        )

        // Lista profili kotów
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 120.dp, bottom = 80.dp, start = 8.dp, end = 8.dp) // Dodano padding boczny
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Odstęp między elementami
        ) {
            items(catProfiles, key = { it.id }) { profile -> // Używamy klucza dla lepszej wydajności
                CatProfileCard(
                    profile = profile,
                    onClick = {
                        selectedCat = profile
                        showDetailsDialog = true
                    }
                )
            }
        }

        // Przycisk dodawania nowego profilu
        FloatingActionButton(
            onClick = {
                editingCat = null // Ustawiamy null, bo dodajemy nowego kota
                showAddEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = colorResource(id = R.color.rozowy)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Dodaj kota",
                tint = colorResource(id = R.color.kremowy)
            )
        }

        // Dialog dodawania/edycji profilu
        if (showAddEditDialog) {
            AddEditCatDialog(
                cat = editingCat, // Przekazujemy kota do edycji lub null dla nowego
                onDismiss = { showAddEditDialog = false },
                onSave = { catToSave ->
                    viewModel.upsertCat(catToSave) // Zapisujemy/aktualizujemy w bazie przez ViewModel
                    showAddEditDialog = false
                    editingCat = null // Resetujemy stan edycji
                }
            )
        }

        // Dialog szczegółów profilu
        if (showDetailsDialog) {
            selectedCat?.let { cat ->
                CatDetailsDialog(
                    cat = cat,
                    onDismiss = { showDetailsDialog = false },
                    onEdit = {
                        editingCat = cat // Ustawiamy kota do edycji
                        showDetailsDialog = false // Zamykamy dialog szczegółów
                        showAddEditDialog = true // Otwieramy dialog edycji
                    },
                    onDelete = {
                        viewModel.deleteCat(cat) // Usuwamy z bazy przez ViewModel
                        showDetailsDialog = false
                        selectedCat = null // Resetujemy wybranego kota
                    },
                    onAddMedicalEvent = {
                        // Nie zamykamy dialogu szczegółów, otwieramy dialog dodawania wpisu
                        showAddMedicalEventDialog = true
                    },
                    onDeleteMedicalEvent = { eventId ->
                        viewModel.removeMedicalEventFromCat(cat.id, eventId)
                    }
                )
            }
        }

        LaunchedEffect(catProfiles) {
            selectedCat?.let { currentSelected ->
                val updatedCat = catProfiles.find { it.id == currentSelected.id }
                selectedCat = updatedCat
            }
        }

        // Dialog dodawania wpisu medycznego
        if (showAddMedicalEventDialog) {
            selectedCat?.let { cat ->
                AddMedicalEventDialog(
                    onDismiss = { showAddMedicalEventDialog = false },
                    onSave = { newEvent ->
                        viewModel.addMedicalEventToCat(cat.id, newEvent)
                        showAddMedicalEventDialog = false
                    }
                )
            }
        }
    }
}

// Karta profilu kota na liście
@Composable
fun CatProfileCard(profile: CatEntity, onClick: () -> Unit) {
    // Bezpieczne obliczanie wieku
    val ageString = try {
        val age = Period.between(profile.birthDate, LocalDate.now())
        "${age.years} lat, ${age.months} mies."
    } catch (e: Exception) {
        "Nieznany wiek" // Obsługa błędu, jeśli data jest nieprawidłowa
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            //.padding(horizontal = 8.dp) // Usunięto padding, bo jest w LazyColumn
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.rozowy)) // Ustawienie koloru tła karty
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colorResource(id = R.color.kremowy),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${profile.breed}, $ageString",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colorResource(id = R.color.kremowy)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Waga: ${profile.weight} kg",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colorResource(id = R.color.kremowy)
                )
            )
        }
    }
}

// Dialog do dodawania lub edycji kota
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCatDialog(cat: CatEntity?, onDismiss: () -> Unit, onSave: (CatEntity) -> Unit) {
    val context = LocalContext.current
    val initialBirthDate = cat?.birthDate ?: LocalDate.now()
    var name by remember { mutableStateOf(cat?.name ?: "") }
    var breed by remember { mutableStateOf(cat?.breed ?: "") }
    var birthDate by remember { mutableStateOf(initialBirthDate) }
    var weight by remember { mutableStateOf(cat?.weight?.toString() ?: "") }
    var allergies by remember { mutableStateOf(cat?.allergies ?: "") }
    var notes by remember { mutableStateOf(cat?.notes ?: "") }
    // Stan dla historii medycznej - na razie uproszczony, można rozbudować
    var medicalHistory by remember { mutableStateOf(cat?.medicalHistory ?: emptyList()) }

    // Formatter daty
    val dateFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE } // Standardowy format RRRR-MM-DD

    // Funkcja do pokazywania DatePickerDialog
    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = java.sql.Date.valueOf(birthDate.toString()) // Ustawienie aktualnej daty

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                birthDate = LocalDate.of(year, month + 1, dayOfMonth) // Miesiące są 0-indeksowane
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), // Zmniejszono padding, aby dialog nie był za duży
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.brazowy)) // Kolor tła dialogu
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()), // Dodano przewijanie
                verticalArrangement = Arrangement.spacedBy(12.dp) // Zwiększono odstępy
            ) {
                Text(
                    text = if (cat == null) "Dodaj nowego kota" else "Edytuj profil kota",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = colorResource(id = R.color.kremowy),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                OutlinedTextField( // Zmieniono na OutlinedTextField dla lepszego wyglądu
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Imię", color = colorResource(id = R.color.kremowy)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors( // Dostosowanie kolorów
                        cursorColor = colorResource(id = R.color.rozowy),
                        focusedBorderColor = colorResource(id = R.color.rozowy),
                        unfocusedBorderColor = colorResource(id = R.color.kremowy),
                    )
                )

                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Rasa", color = colorResource(id = R.color.kremowy)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = colorResource(id = R.color.rozowy),
                        focusedBorderColor = colorResource(id = R.color.rozowy),
                        unfocusedBorderColor = colorResource(id = R.color.kremowy),
                    )
                )

                // Pole daty urodzenia z ikonką kalendarza
                OutlinedTextField(
                    value = birthDate.format(dateFormatter), // Wyświetlanie sformatowanej daty
                    onValueChange = { /* Odczyt tylko do wyświetlania */ },
                    label = { Text("Data urodzenia", color = colorResource(id = R.color.kremowy)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true, // Pole tylko do odczytu
                    trailingIcon = { // Ikona do otwierania DatePicker
                        IconButton(onClick = { showDatePicker() }) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Wybierz datę",
                                tint = colorResource(id = R.color.rozowy)
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = colorResource(id = R.color.kremowy), // Kolor tekstu gdy readOnly
                        cursorColor = colorResource(id = R.color.rozowy),
                        focusedBorderColor = colorResource(id = R.color.rozowy),
                        unfocusedBorderColor = colorResource(id = R.color.kremowy),
                        disabledBorderColor = colorResource(id = R.color.kremowy) // Kolor ramki gdy readOnly
                    )
                )


                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { char -> char.isDigit() || char == '.' } }, // Akceptuj tylko cyfry i kropkę
                    label = { Text("Waga (kg)", color = colorResource(id = R.color.kremowy)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = colorResource(id = R.color.rozowy),
                        focusedBorderColor = colorResource(id = R.color.rozowy),
                        unfocusedBorderColor = colorResource(id = R.color.kremowy),
                    )
                )

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Alergie", color = colorResource(id = R.color.kremowy)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = colorResource(id = R.color.rozowy),
                        focusedBorderColor = colorResource(id = R.color.rozowy),
                        unfocusedBorderColor = colorResource(id = R.color.kremowy),
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notatki", color = colorResource(id = R.color.kremowy)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp), // Minimalna wysokość dla notatek
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = colorResource(id = R.color.rozowy),
                        focusedBorderColor = colorResource(id = R.color.rozowy),
                        unfocusedBorderColor = colorResource(id = R.color.kremowy),
                    )
                )


                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End // Przyciski na końcu
                ) {
                    TextButton(onClick = onDismiss) { // Przycisk anulowania
                        Text("Anuluj", color = colorResource(id = R.color.kremowy))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button( // Przycisk zapisu
                        onClick = {
                            val currentWeight = weight.toDoubleOrNull() ?: 0.0
                            // Walidacja (prosta)
                            if (name.isNotBlank() && breed.isNotBlank()) {
                                val catToSave = CatEntity(
                                    id = cat?.id ?: 0, // Użyj istniejącego ID lub 0 dla nowego (Room wygeneruje)
                                    name = name.trim(),
                                    breed = breed.trim(),
                                    birthDate = birthDate,
                                    weight = currentWeight,
                                    allergies = allergies.trim(),
                                    medicalHistory = medicalHistory, // Na razie bez zmian
                                    notes = notes.trim()
                                )
                                onSave(catToSave)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.rozowy))
                    ) {
                        Text("Zapisz", color = colorResource(id = R.color.kremowy))
                    }
                }
            }
        }
    }
}

// Dialog pokazujący szczegóły kota
@Composable
fun CatDetailsDialog(
    cat: CatEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddMedicalEvent: () -> Unit, // Nowy callback
    onDeleteMedicalEvent: (String) -> Unit // Nowy callback
) {
    val context = LocalContext.current
    val ageString = try {
        val age = Period.between(cat.birthDate, LocalDate.now())
        "${age.years} lat, ${age.months} mies."
    } catch (e: Exception) { "Nieznany wiek" }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.brazowy))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sekcja podstawowych informacji (bez zmian)
                Text(
                    text = cat.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = colorResource(id = R.color.kremowy), fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Divider(color = colorResource(id = R.color.rozowy_jasny), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                DetailRow(label = "Rasa", value = cat.breed)
                DetailRow(label = "Wiek", value = ageString)
                DetailRow(label = "Data ur.", value = cat.birthDate.format(dateFormatter))
                DetailRow(label = "Waga", value = "${cat.weight} kg")
                DetailRow(label = "Alergie", value = cat.allergies.ifBlank { "Brak" })
                DetailRow(label = "Notatki", value = cat.notes.ifBlank { "Brak" })

                Spacer(modifier = Modifier.height(16.dp))

                // Sekcja historii medycznej (zmodyfikowana)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween // Rozsuwa tytuł i przycisk
                ) {
                    Text(
                        text = "Historia medyczna:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colorResource(id = R.color.kremowy), fontWeight = FontWeight.Bold
                        )
                    )
                    // *** NOWY PRZYCISK ***
                    IconButton(onClick = onAddMedicalEvent, modifier = Modifier.size(32.dp)) { // Mniejszy przycisk
                        Icon(
                            imageVector = Icons.Filled.AddCircle, // Ikona dodawania
                            contentDescription = "Dodaj wpis medyczny",
                            tint = colorResource(id = R.color.rozowy)
                        )
                    }
                }
                Divider(color = colorResource(id = R.color.rozowy_jasny), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp)) // Usunięto górny padding

                if (cat.medicalHistory.isEmpty()) {
                    Text(
                        text = "Brak wpisów",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colorResource(id = R.color.kremowy)),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(start = 8.dp)) {
                        // Sortowanie od najnowszych
                        cat.medicalHistory.sortedByDescending { it.date }.forEach { event ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text( // Tekst wpisu zajmuje dostępną przestrzeń
                                    text = "${event.date.format(dateFormatter)}: ${event.type.uppercase()} - ${event.description}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = colorResource(id = R.color.kremowy)),
                                    modifier = Modifier.weight(1f) // Pozwala tekstowi się rozciągnąć
                                )
                                IconButton(
                                    onClick = { onDeleteMedicalEvent(event.id) },
                                    modifier = Modifier.size(24.dp) // Mały przycisk usuwania
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Usuń wpis",
                                        tint = colorResource(id = R.color.czerwony_alert) // Kolor ostrzegawczy
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Przyciski akcji Edytuj/Usuń (bez zmian)
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.kremowy)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Edytuj", tint = colorResource(id = R.color.ciemny))
                        Spacer(Modifier.width(4.dp))
                        Text("Edytuj", color = colorResource(id = R.color.ciemny))
                    }
                    Button(
                        onClick = {
                            showDeleteConfirmationDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.czerwony_alert)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Usuń", tint = colorResource(id = R.color.kremowy))
                        Spacer(Modifier.width(4.dp))
                        Text("Usuń", color = colorResource(id = R.color.kremowy))
                    }
                }
                // Przycisk Zamknij (bez zmian)
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                ) {
                    Text("Zamknij", color = colorResource(id = R.color.kremowy))
                }
            }
        }
        if (showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationDialog = false },
                title = { Text("Potwierdzenie usunięcia") },
                text = { Text("Czy na pewno chcesz usunąć profil kota \"${cat.name}\"? Tej operacji nie można cofnąć.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmationDialog = false
                            onDelete()
                        }
                    ) {
                        Text("Usuń", color = colorResource(id = R.color.czerwony_alert))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}
// Pomocniczy Composable do wyświetlania wiersza szczegółów (etykieta + wartość)
@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = colorResource(id = R.color.kremowy), // Kolor etykiety
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.width(100.dp) // Stała szerokość etykiety dla wyrównania
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = colorResource(id = R.color.kremowy) // Kolor wartości
            )
        )
    }
}

// Dialog do dodawania nowego wpisu medycznego
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicalEventDialog(
    onDismiss: () -> Unit,
    onSave: (MedicalEventEntity) -> Unit
) {
    val context = LocalContext.current
    var eventType by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(LocalDate.now()) }
    var eventDescription by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Dostępne typy zdarzeń (można rozszerzyć)
    val eventTypes = listOf("Wizyta u weterynarza", "Szczepienie", "Odrobaczanie", "Operacja", "Inne")
    var expandedDropdown by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    // Funkcja do pokazywania DatePickerDialog
    fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        calendar.time = java.sql.Date.valueOf(eventDate.toString())
        DatePickerDialog(
            context, { _, year, month, dayOfMonth -> eventDate = LocalDate.of(year, month + 1, dayOfMonth) },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.brazowy))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Dodaj wpis medyczny",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = colorResource(id = R.color.kremowy), fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Dropdown do wyboru typu zdarzenia
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = eventType,
                        onValueChange = {}, // Puste, bo wybieramy z listy
                        readOnly = true,
                        label = { Text("Typ zdarzenia", color = colorResource(id = R.color.kremowy)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(), // Ważne dla ExposedDropdownMenuBox
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = colorResource(id = R.color.kremowy),
                            cursorColor = colorResource(id = R.color.rozowy), focusedBorderColor = colorResource(id = R.color.rozowy),
                            unfocusedBorderColor = colorResource(id = R.color.kremowy), disabledBorderColor = colorResource(id = R.color.kremowy)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier.background(colorResource(id = R.color.brazowy)) // Tło menu
                    ) {
                        eventTypes.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption, color = colorResource(id = R.color.kremowy)) },
                                onClick = {
                                    eventType = selectionOption
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }


                // Pole daty zdarzenia z ikonką kalendarza
                OutlinedTextField(
                    value = eventDate.format(dateFormatter),
                    onValueChange = { },
                    label = { Text("Data zdarzenia", color = colorResource(id = R.color.kremowy)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePickerDialog() }) {
                            Icon(Icons.Default.DateRange, "Wybierz datę", tint = colorResource(id = R.color.rozowy))
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = colorResource(id = R.color.kremowy),
                        cursorColor = colorResource(id = R.color.rozowy), focusedBorderColor = colorResource(id = R.color.rozowy),
                        unfocusedBorderColor = colorResource(id = R.color.kremowy), disabledBorderColor = colorResource(id = R.color.kremowy)
                    )
                )

                // Pole opisu zdarzenia
                OutlinedTextField(
                    value = eventDescription,
                    onValueChange = { eventDescription = it },
                    label = { Text("Opis", color = colorResource(id = R.color.kremowy)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp), // Minimalna wysokość
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = colorResource(id = R.color.rozowy),
                        focusedBorderColor = colorResource(id = R.color.rozowy), unfocusedBorderColor = colorResource(id = R.color.kremowy),
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Przyciski Anuluj/Zapisz
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj", color = colorResource(id = R.color.kremowy))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Prosta walidacja
                            if (eventType.isNotBlank() && eventDescription.isNotBlank()) {
                                val newEvent = MedicalEventEntity(
                                    id = UUID.randomUUID().toString(), // Generowanie unikalnego ID
                                    type = eventType,
                                    date = eventDate,
                                    description = eventDescription.trim()
                                )
                                onSave(newEvent)
                            } else {
                                // TODO: Pokaż komunikat o błędzie (np. Snackbar)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.rozowy))
                    ) {
                        Text("Zapisz", color = colorResource(id = R.color.kremowy))
                    }
                }
            }
        }
    }
}
