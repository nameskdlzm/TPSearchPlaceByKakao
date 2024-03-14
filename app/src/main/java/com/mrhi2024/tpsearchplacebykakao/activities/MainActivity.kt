package com.mrhi2024.tpsearchplacebykakao.activities

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mrhi2024.tpsearchplacebykakao.R
import com.mrhi2024.tpsearchplacebykakao.data.KakaoSearchPlaceResponse
import com.mrhi2024.tpsearchplacebykakao.data.Place
import com.mrhi2024.tpsearchplacebykakao.data.PlaceMeta
import com.mrhi2024.tpsearchplacebykakao.databinding.ActivityMainBinding
import com.mrhi2024.tpsearchplacebykakao.fragments.PlaceFavorFragment
import com.mrhi2024.tpsearchplacebykakao.fragments.PlaceListFragment
import com.mrhi2024.tpsearchplacebykakao.fragments.PlaceMapFragment
import com.mrhi2024.tpsearchplacebykakao.network.RetrofitHelper
import com.mrhi2024.tpsearchplacebykakao.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    // 카카오 검색에 필요한 요청 데이터 : query(검색어), x(경도-longitude), y(위도-latitude)
    //1. 검색장소명
    var searchQuery:String = "화장실" // 앱 초기 검색어 - 내 주변 개방 화장실

    //2. 현재 내위치 정보 객체(위도,경도 정보를 멤버로 보유)
    var myLocation:Location? = null

    // [ Google Fused Location API 사용 : play-services-location ]
    val locationProviderClient:FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    // kakao search API 응답결과 객체 참조변수
    var searchPlaceResponse:KakaoSearchPlaceResponse?=null

    //============================================================================================================================= 멤버변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 처음 보여질 Fragment를 화면에 붙이기
        supportFragmentManager.beginTransaction().add(R.id.container_fragment,PlaceListFragment()).commit()

        // bnv의 선택에 따라 Fragment를 동적으로 교체
        binding.bnv.setOnItemSelectedListener{

            when(it.itemId){
                R.id.menu_bnv_list -> supportFragmentManager.beginTransaction().replace(R.id.container_fragment,PlaceListFragment()).commit()
                R.id.menu_bnv_map -> supportFragmentManager.beginTransaction().replace(R.id.container_fragment,PlaceMapFragment()).commit()
                R.id.menu_bnv_favor -> supportFragmentManager.beginTransaction().replace(R.id.container_fragment,PlaceFavorFragment()).commit()
                R.id.menu_bnv_option -> Toast.makeText(this, "Option", Toast.LENGTH_SHORT).show()
            }

            true // OnItemSelectedListener의 추상메소드는 리턴값을 가지고 있음. SAM변환을 하면 return키워드를 사용하면 안됨

        }

        // bnv의 아이템 선택 리플효과의 범위를 제한하지 않기위해 배경 영역을 없애기
        binding.bnv.background = null



        // 소프트 키보드의 검색버튼을 클릭하였을 때.
        binding.etSearch.setOnEditorActionListener{v,actionId,event ->
            searchQuery = binding.etSearch.text.toString()
            // 키워드로 장소검색 요청
            searchPlaces()

            // 액션버튼이 클릭되었을때 여기서 모든 액션을 소비하지 않았다는 뜻
            false
        }

        // 특정 키워드 단축 choice 버튼들에 리스너 처리하는 코드를 별도의 메소드에 작성
        setChoiceButtonsListener()

        // 위치정보 제공에 대한 퍼미션 체크
        val permissionState:Int = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionState == PackageManager.PERMISSION_DENIED){
            // 퍼미션을 요청하는 다이얼로그 보이고 그 결과를 받아오는 작업을 대신해주는 대행사 이용
            permissionResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }else{
            // 위치정보수집이 허가되어 있다면 곧바로 위치정보 얻어오는 작업 시작
            requestMyLocation()
        }

        // 내위치 갱신버튼 클릭처리
        binding.toolbar.setNavigationOnClickListener { requestMyLocation() }

        // 새로고침 버튼 클릭처리
        binding.fabRefresh.setOnClickListener {
            requestMyLocation()

            ObjectAnimator.ofFloat(it,"translationY",-140f).start()
            ObjectAnimator.ofFloat(it,"rotationX",360f).start()
        }


    }// on Create

    // 카카오 로컬 API를 활용하여 키워드로 장소를 검색하는 기능 메소드
    private fun searchPlaces(){
        Toast.makeText(this, "$searchQuery\n${myLocation?.latitude},${myLocation?.longitude}", Toast.LENGTH_SHORT).show()

        //레트로핏을 이용한 REST API 작업 수행 - GET방식
        val retrofit = RetrofitHelper.getRetrofitInstance("https://dapi.kakao.com")
        val retrofitService = retrofit.create(RetrofitService::class.java)
        val call =retrofitService.searchPlace(searchQuery,myLocation?.longitude.toString(),myLocation?.latitude.toString())
        call.enqueue(object : Callback<KakaoSearchPlaceResponse>{
            override fun onResponse(
                call: Call<KakaoSearchPlaceResponse>,
                response: Response<KakaoSearchPlaceResponse>
            ) {
                // 응답받은 json을 파싱한 객체를 참조하기
                searchPlaceResponse = response.body()

                // 먼저 data가 온전히 잘 왔는지 파악해보기
                val meta:PlaceMeta? = searchPlaceResponse?.meta
                val documents:List<Place>? = searchPlaceResponse?.  documents

                //AlertDialog.Builder(this@MainActivity).setMessage("${meta?.total_count}\n${documents?.get(0)?.place_name}").create().show()

                // 무조건 검색이 완료되면 '리스트' 형태로 먼저 보여주도록 할 것임
                binding.bnv.selectedItemId = R.id.menu_bnv_list

                // fab 버튼 원위치
//
            }

            override fun onFailure(call: Call<KakaoSearchPlaceResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
            }

        })

//        val call = retrofitService.searchPlaceToString(searchQuery,myLocation?.longitude.toString(),myLocation?.latitude.toString())
//        call.enqueue(object : Callback<String>{
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                val s= response.body()
//                AlertDialog.Builder(this@MainActivity).setMessage(s).create().show()
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                Toast.makeText(this@MainActivity, "error:${t.message}", Toast.LENGTH_SHORT).show()
//            }
//
//        })


    }

    private fun setChoiceButtonsListener(){
        binding.layoutChoice.choice01.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice02.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice03.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice04.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice05.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice06.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice07.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice08.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice09.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice10.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice11.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice12.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice13.setOnClickListener { clickChoice(it) }
    }

    // 멤버변수(property)
    var choiceID = R.id.choice01

    private fun clickChoice(view:View){

        // 기존에 선택되었던 ImageView를 찾아서 배경이미지를 선택되지 않는 하얀색의 원그림으로 변경
        findViewById<ImageView>(choiceID).setBackgroundResource(R.drawable.bg_choice)

        //현재 클릭한 ImageView의 배경을 선택된 회색 원그림으로 변경
        view.setBackgroundResource(R.drawable.bg_choice_selected)

        // 클릭한 뷰의 id를 저장
        choiceID = view.id

        when(choiceID){
            R.id.choice01->searchQuery = "화장실"
            R.id.choice02->searchQuery = "약국"
            R.id.choice03->searchQuery = "주유소"
            R.id.choice04->searchQuery = "전기차충전소"
            R.id.choice05->searchQuery = "주차장"
            R.id.choice06->searchQuery = "공원"
            R.id.choice07->searchQuery = "편의점"
            R.id.choice08->searchQuery = "맛집"
            R.id.choice09->searchQuery = "병원"
            R.id.choice10->searchQuery = "버스정거장"
            R.id.choice11->searchQuery = "영화관"
            R.id.choice12->searchQuery = "도서관"
            R.id.choice13->searchQuery = "마트"
        }

        // 바뀐 검색장소명으로 검색을 요청
        searchPlaces()

        // 검색창에 글씨가 있다면 지우기
        binding.etSearch.text.clear()
        binding.etSearch.clearFocus()

    }

    // 현재 위치를 얻어오는 작업 요청 코드가 있는 기능메소드
    private fun requestMyLocation(){

        // 요청 객체 생성
        val request:LocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,3000).build()

        // 실시간 위치정보 갱신 요청 - 퍼미션 체크코드가 있어야만 함.
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        locationProviderClient.requestLocationUpdates(request,locationCallback,Looper.getMainLooper())
    }

    // 위치 정보 갱신때마다 발동하는 콜백 객체
    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            myLocation = p0.lastLocation

            // 위치 탐색이 종료되었으니 내 위치 정보 업데이트를 그만불러오기
            locationProviderClient.removeLocationUpdates(this) // 'this'는 location callback 객체

            // 위치정보를 얻었으니 키워드 장소검색 작업 시작
            searchPlaces()

        }
    }

    // 퍼미션요청 및 결과를 받아오는 작업을 대신하는 대행사 등록
    //퍼미션 요청 및 결과를 받아오는 작업을 대신하는 대행사 등록
    private val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) requestMyLocation()
        else Toast.makeText(this, "내 위치정보를 제공하지 않아 검색기능 사용이 제한됩니다.", Toast.LENGTH_SHORT).show()
    }

}