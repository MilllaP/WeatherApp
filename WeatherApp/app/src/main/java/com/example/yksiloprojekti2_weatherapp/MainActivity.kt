package com.example.yksiloprojekti2_weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

import com.example.yksiloprojekti2_weatherapp.viewmodel.WeatherViewModel
import com.example.yksiloprojekti2_weatherapp.ui.theme.WeatherAppTheme
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    // ViewModel instance that will be used to manage and fetch weather data
    private val viewModel: WeatherViewModel by viewModels()

    // The onCreate method is called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting the content of the activity using Jetpack Compose
        setContent {
            // Using custom WeatherAppTheme
            WeatherAppTheme(dynamicColor = false) {
                // Setting up navigation for the app
                val navController = rememberNavController()
                AppNavigation(navController = navController, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, viewModel: WeatherViewModel) {
    // Setting up the navigation host with a start destination
    NavHost(navController = navController, startDestination = "Home") {

        // Home Screen
        composable("Home") {
            Scaffold { innerPadding ->
                WeatherScreen(
                    viewModel = viewModel,
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Details Screen
        composable("details") {
            DetailsScreen(navController = navController, viewModel = viewModel)
        }

        // About Screen
        composable("about") {
            AboutPage(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel, navController: NavHostController,  modifier: Modifier = Modifier) {
    val weather = viewModel.weatherData.observeAsState() // Observes weather data from ViewModel
    val isLoading by viewModel.isLoading.observeAsState(false) // Observes loading state

    var cityInput by remember { mutableStateOf("") } // State for user input city name
    val errorMessage by viewModel.errorMessage.observeAsState() // Observes error message from ViewModel

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Sääsovellus")
                },
                actions = {
                    Box(modifier = Modifier.size(200.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Cloud,
                            contentDescription = "Cloud Icon",
                            modifier = Modifier
                                .size(90.dp)
                                .align(Alignment.CenterEnd)
                                .offset(y = (20).dp),
                            tint = Color(0xFF47B4D7),
                        )
                        Icon(
                            imageVector = Icons.Filled.WbSunny,
                            contentDescription = "Sun Icon",
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.CenterEnd)
                                .offset(x = (-40).dp),
                            tint = Color(0xFFFFED4C)
                        )
                    }
                }
            )
        }
    )  { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp)
        ) {
            // TextField for user to input city name
            OutlinedTextField(
                value = cityInput,
                onValueChange = { cityInput = it },
                label = { Text("Kaupunki") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.searchCityWeather(cityInput)
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Hea kaupunki")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            // Shows error message if city is not found
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // Weather display
                weather.value?.let { currentWeather ->
                    WeatherItem(
                        cityName = currentWeather.cityName,
                        time = currentWeather.time,
                        temperature = currentWeather.temperature_2m,
                        windspeed = currentWeather.wind_speed_10m
                    )
                } ?: Text("Tietoja ei saatavilla", style = MaterialTheme.typography.bodyLarge)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
            ) {

                // Button to navigate to the details screen
                Button(onClick = { navController.navigate("details") }) {
                    Text("Tuntiennuste")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button to navigate to the about screen
                Button(onClick = { navController.navigate("about") }) {
                    Text("Tietoja")
                }
            }
        }
    }
}

@Composable
fun WeatherItem(cityName: String, time: String, temperature: Double, windspeed: Double) {
    val formattedTime = time.substring(11, 16) // Extracts time from timestamp string

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shadowElevation = 2.dp
    ) {
        // Displays weather information for the current day
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "$cityName klo $formattedTime", style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Thermostat,
                    contentDescription = "Temperature Icon",
                    tint = Color(0xFFFF6666)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$temperature°C",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Air,
                    contentDescription = "Wind Icon",
                    tint = Color(0xFF4F98CA)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$windspeed km/h",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// Preview for previewing UI in Android Studio without running the app on a device or emulator
@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    WeatherAppTheme {
        WeatherScreen(
            viewModel = WeatherViewModel(),
            navController = rememberNavController()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(navController: NavHostController, viewModel: WeatherViewModel) {
    // State to keep track of which day's data is selected (Today or Tomorrow)
    val selectedDay = remember { mutableStateOf("Tänään") }
    val hourlyWeatherState = viewModel.hourlyWeatherData.observeAsState() // Observes hourly weather data from ViewModel

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Etusivulle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Tuntikohtaiset sääennusteet",
                    style = MaterialTheme.typography.headlineMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Buttons to switch between today and tomorrow's weather
                    Button(
                        onClick = { selectedDay.value = "Tänään" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tänään")
                    }
                    // Button to switch between today and tomorrow's weather
                    Button(
                        onClick = { selectedDay.value = "Huomenna" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Huomenna")
                    }
                }

                // Display the selected day weather data
                Text(
                    text = "Säädata: ${selectedDay.value}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )

                // Display the hourly weather data
                hourlyWeatherState.value?.let { hourlyWeather ->
                    val filteredData = viewModel.getTodaysAndTomorrowsWeather(hourlyWeather)

                    // Filter data based on selected day
                    val selectedData = filteredData.filter { weather ->
                        val date = LocalDateTime.parse(weather.time.first(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                        val currentDate = LocalDate.now()
                        val tomorrowDate = currentDate.plusDays(1)
                        (selectedDay.value == "Tänään" && date.toLocalDate().isEqual(currentDate)) ||
                                (selectedDay.value == "Huomenna" && date.toLocalDate().isEqual(tomorrowDate))
                    }.flatMap { it.time.zip(it.temperature_2m) }

                    LazyColumn(
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        items(selectedData) { (time, temperature) ->
                            HourlyWeatherItem(time = time, temperature = temperature)
                        }
                    }
                } ?: run {
                    Text(
                        "Ladataan tuntikohtaisia sääennusteita...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun HourlyWeatherItem(time: String, temperature: Double) {
    // Parses the time to handle the "T" between date and time
    val formattedTime = try {
        LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
    } catch (e: Exception) {
        time
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Aika: $formattedTime", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Lämpötila: $temperature°C", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Etusivulle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    )  { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // About title
            Text(
                text = "Tietoa sovelluksesta",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // About text
            Text(
                text = "Sovellus hakee sääennusteen Open-Meteo -palvelusta ja paikantaa kaupungin koordinaatit Nominatim-palvelun avulla. " +
                        "Voit hakea sääennusteen syöttämällä kaupungin nimen hakukenttään. " +
                        "Sovellus näyttää nykyisen säätilan ja seuraavan 48 tunnin sääennusteen. " +
                        "Aika muutetaan automaattisesti paikalliseen aikaan Open-Mateo -palvelun haussa. " +
                        "Etusivun lämpötila päivittyy 15 minuutin välein.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Bullet points
            Text(
                text = "Pääominaisuudet:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BulletList(
                items = listOf(
                    "Hae sääennuste kirjoittamalla kaupungin nimi hakukenttään.",
                    "Tämän hetkisen säätilan näen etusivulta ja tuntikohtaiset ennusteet näet Tuntiennuste-sivulta.",
                )
            )
        }
    }
}

@Composable
fun BulletList(items: List<String>) {
    // Displays a list of bullet points
    Column {
        for (item in items) {
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("•", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp))
                Text(text = item, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
