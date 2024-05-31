/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.EntreeMenuScreen
import com.example.lunchtray.ui.OrderViewModel
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen

enum class AppRoutes(@StringRes val title: Int) {
    Start(title = R.string.start_order),
    Entree_menu(title = R.string.choose_entree),
    Side_dish_menu(title = R.string.choose_side_dish),
    Accompaniment_menu(title = R.string.choose_accompaniment),
    Checkout(title = R.string.order_checkout)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentScreen: AppRoutes,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(id = currentScreen.title)) },
        modifier = modifier.fillMaxWidth(),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentScreen = AppRoutes.valueOf(
        backStackEntry?.destination?.route ?: AppRoutes.Start.name
    )

    val viewModel: OrderViewModel = viewModel()

    Scaffold(
        topBar = {
            AppBar(
                currentScreen,
                canNavigateBack = navController.previousBackStackEntry !== null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = AppRoutes.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppRoutes.Start.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = { navController.navigate(AppRoutes.Entree_menu.name) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                )
            }

            composable(route = AppRoutes.Entree_menu.name) {
                EntreeMenuScreen(
                    options = DataSource.entreeMenuItems,
                    onCancelButtonClicked = {
                        resetOrderAndNavigateToStart(
                            viewModel,
                            navController
                        )
                    },
                    onNextButtonClicked = { navController.navigate(AppRoutes.Side_dish_menu.name) },
                    onSelectionChanged = { viewModel.updateEntree(it) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }

            composable(route = AppRoutes.Side_dish_menu.name) {
                SideDishMenuScreen(
                    options = DataSource.sideDishMenuItems,
                    onCancelButtonClicked = {
                        resetOrderAndNavigateToStart(
                            viewModel,
                            navController
                        )
                    },
                    onNextButtonClicked = { navController.navigate(AppRoutes.Accompaniment_menu.name) },
                    onSelectionChanged = { viewModel.updateSideDish(it) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }

            composable(route = AppRoutes.Accompaniment_menu.name) {
                AccompanimentMenuScreen(
                    options = DataSource.accompanimentMenuItems,
                    onCancelButtonClicked = {
                        resetOrderAndNavigateToStart(
                            viewModel,
                            navController
                        )
                    },
                    onNextButtonClicked = { navController.navigate(AppRoutes.Checkout.name) },
                    onSelectionChanged = { viewModel.updateAccompaniment(it) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }

            composable(route = AppRoutes.Checkout.name) {
                CheckoutScreen(
                    orderUiState = uiState,
                    onNextButtonClicked = {
                        viewModel.resetOrder()
                        navController.popBackStack(AppRoutes.Start.name, false)
                    },
                    onCancelButtonClicked = {
                        resetOrderAndNavigateToStart(
                            viewModel,
                            navController
                        )
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }
        }
    }
}

fun resetOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(AppRoutes.Start.name, false)
}
