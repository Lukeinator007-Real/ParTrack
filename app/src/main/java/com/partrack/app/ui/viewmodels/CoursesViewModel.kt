package com.partrack.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.partrack.app.data.Course
import com.partrack.app.data.CourseDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CoursesViewModel(private val courseDao: CourseDao) : ViewModel() {

    val courses: Flow<List<Course>> = courseDao.getAllCourses()

    fun addCourse(name: String, holes: Int, pars: List<Int>) {
        viewModelScope.launch {
            val newCourse = Course(
                name = name,
                holes = holes,
                pars = pars
            )
            courseDao.insertCourse(newCourse)
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            courseDao.deleteCourse(course)
        }
    }
}

class CoursesViewModelFactory(private val courseDao: CourseDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoursesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoursesViewModel(courseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
