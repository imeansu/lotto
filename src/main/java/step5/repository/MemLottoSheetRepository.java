package step5.repository;

import step2.domain.Lotto;
import step2.domain.LottoSheetWithId;
import step2.dto.LottoResultDto;
import step2.repository.LottoSheetRepository;
import step3.util.TypeConverter;
import step3.util.Validator;
import step5.data.DataSource;
import step5.data.DataSourceUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemLottoSheetRepository implements LottoSheetRepository {

    // userId 발급을 위한 키 (Auto Increment 같은) 유저 id 테이블을 만들지 않아서 임시로..
    private static Long ID_KEY = 0L;

    // DB 정보를 가지는 객체
    private final DataSource dataSource;

    public MemLottoSheetRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        loadLottoTable();
    }

    @Override
    public LottoSheetWithId save(LottoSheetWithId lottoSheet) {
        // userId 와 Lotto의 toString([1, 2, 3, 4, 5, 6])을 저장
        String sql = "insert into lotto(userId, lotto) values(?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        lottoSheet.setId(++ID_KEY);

        for (Lotto lotto : lottoSheet.getLottoList()){
            try {
                conn = getConnection();
                pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, lottoSheet.getId().toString());
                pstmt.setString(2, lotto.toString());

                pstmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                close(conn, pstmt, rs);
            }
        }

        return lottoSheet;
    }

    // 로또 결과 저장 로직 미구현..
    public LottoSheetWithId update(LottoResultDto lottoResultDto){
        String sql = "UPDATE lotto SET result = ? where (id = ? and numbers = ?)";
        return null;
    }

    @Override
    public LottoSheetWithId findByUserId(Long userId) {
        // userId 로 다수의 Lotto 개를 가져와 기존에 처리(저장)하던 LottoSheetWithId를 생성
        String sql = "select * from lotto where userId = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId.toString());

            rs = pstmt.executeQuery();

            LottoSheetWithId lottoSheetWithId = new LottoSheetWithId();
            lottoSheetWithId.setId(userId);

            List<Lotto> lottoList = new ArrayList<>();
            while (rs.next()) {
                String numberStr = rs.getString("lotto");
                Lotto lotto = new Lotto(TypeConverter.DB_LOTTO_CONVERT(numberStr));
                lottoList.add(lotto);
            }

            lottoSheetWithId.setLottoList(lottoList);

            return lottoSheetWithId;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }

        throw new RuntimeException();
    }

    private Connection getConnection() throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs){
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) {
                close(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void close(Connection conn) throws SQLException {
        DataSourceUtils.releaseConnection(conn, dataSource);
    }

    // Lotto 테이블 생성 DDL
    private void loadLottoTable() {
        Connection conn = null;
        Statement statement = null;

        String sql = "drop table if exists lotto CASCADE;\n" +
                "CREATE TABLE lotto (" +
                "lottoId   bigint primary key auto_increment, " +
                "userId VARCHAR(10) NOT NULL, " +
                "lotto VARCHAR(50) NOT NULL, " +
                "result VARCHAR(50) " +
                ");";

        try {
            conn = getConnection();
            statement = conn.createStatement();
            statement.execute(sql);
            statement.close();
            DataSourceUtils.releaseConnection(conn, dataSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
